package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.response.BajaInventarioResponse;
import com.medicalstore.pos.dto.response.OrdenCompraResponse;
import com.medicalstore.pos.dto.response.ReporteBajasResponse;
import com.medicalstore.pos.dto.response.ReporteConsumoResponse;
import com.medicalstore.pos.dto.response.ReporteInventarioResponse;
import com.medicalstore.pos.dto.response.ReporteOrdenesResponse;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exporta los reportes de M08 a PDF (iText 7) y a XLSX (Apache POI).
 *
 * Los dos formatos se construyen a partir de la misma estructura intermedia
 * (titulo, encabezados, filas y totales), de modo que el PDF y la hoja de
 * calculo siempre muestran exactamente los mismos datos (RF-12).
 */
@Service
public class ReporteExportService {
    
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DeviceRgb AZUL = new DeviceRgb(0x00, 0x5D, 0xAC);
    private static final DeviceRgb AZUL_CLARO = new DeviceRgb(0xE8, 0xF0, 0xFA);
    
    /**
     * Tipos de reporte soportados por la exportacion.
     */
    public static final List<String> TIPOS = List.of("consumo", "inventario", "bajas", "ordenes");
    
    private final ReporteService reporteService;
    
    public ReporteExportService(ReporteService reporteService) {
        this.reporteService = reporteService;
    }
    
    /**
     * @return el PDF del reporte indicado.
     */
    public byte[] pdf(String tipo, LocalDate desde, LocalDate hasta, String usuario, String rol) {
        return renderizarPdf(construir(tipo, desde, hasta), usuario, rol);
    }
    
    /**
     * @return el XLSX del reporte indicado.
     */
    public byte[] xlsx(String tipo, LocalDate desde, LocalDate hasta, String usuario, String rol) {
        return renderizarXlsx(construir(tipo, desde, hasta), usuario, rol);
    }
    
    /**
     * Nombre sugerido del archivo descargado.
     */
    public String nombreArchivo(String tipo, String extension) {
        return "farmamed-" + tipo + "-" + LocalDate.now().format(FECHA) + "." + extension;
    }
    
    // ------------------------------------------------------------------
    //  Construccion de la tabla intermedia
    // ------------------------------------------------------------------
    
    private Tabla construir(String tipo, LocalDate desde, LocalDate hasta) {
        return switch (tipo) {
            case "consumo" -> consumo(desde, hasta);
            case "inventario" -> inventario();
            case "bajas" -> bajas(desde, hasta);
            case "ordenes" -> ordenes(desde, hasta);
            default -> throw new IllegalArgumentException("Reporte no soportado: " + tipo);
        };
    }
    
    private Tabla consumo(LocalDate desde, LocalDate hasta) {
        ReporteConsumoResponse r = reporteService.reporteConsumo(desde, hasta);
        Tabla t = new Tabla("Consumo de medicamentos",
                "Periodo: " + desde.format(FECHA) + " al " + hasta.format(FECHA));
        t.encabezados("Codigo", "Medicamento", "Unidad", "Dispensaciones", "Unidades", "Promedio por dispensacion");
        for (ReporteConsumoResponse.ItemConsumoResponse i : r.getItems()) {
            t.fila(i.getCodigo(), i.getNombre(), i.getUnidadMedida(),
                    String.valueOf(i.getDispensaciones()),
                    String.valueOf(i.getUnidadesConsumidas()),
                    String.valueOf(i.getPromedioPorDispensacion()));
        }
        t.totales("TOTAL", "", "",
                String.valueOf(r.getTotalDispensaciones()),
                String.valueOf(r.getTotalUnidades()),
                r.getTotalMedicamentos() + " medicamentos");
        return t;
    }
    
    private Tabla inventario() {
        ReporteInventarioResponse r = reporteService.reporteInventario();
        Tabla t = new Tabla("Inventario de medicamentos", "Vigente y vencido (RN06)");
        t.encabezados("Codigo", "Medicamento", "Unidad", "Proveedor", "Stock vigente", "Stock vencido",
                "Stock minimo", "Stock maximo", "Critico", "Lotes");
        for (ReporteInventarioResponse.ItemInventarioResponse i : r.getItems()) {
            t.fila(i.getCodigo(), i.getNombre(), i.getUnidadMedida(), i.getProveedorRazonSocial(),
                    String.valueOf(i.getStockVigente()), String.valueOf(i.getStockVencido()),
                    String.valueOf(i.getStockMinimo()), String.valueOf(i.getStockMaximo()),
                    Boolean.TRUE.equals(i.getStockCritico()) ? "SI" : "NO",
                    String.valueOf(i.getLotesActivos()));
        }
        t.totales("TOTAL", "", "", "",
                String.valueOf(r.getTotalUnidadesVigentes()),
                String.valueOf(r.getTotalUnidadesVencidas()),
                "", "", r.getMedicamentosStockCritico() + " criticos",
                String.valueOf(r.getTotalLotes()));
        return t;
    }
    
    private Tabla bajas(LocalDate desde, LocalDate hasta) {
        ReporteBajasResponse r = reporteService.reporteBajas(desde, hasta);
        Tabla t = new Tabla("Bajas de inventario",
                "Periodo: " + desde.format(FECHA) + " al " + hasta.format(FECHA));
        t.encabezados("Fecha", "Lote", "Medicamento", "Motivo", "Cantidad", "Valor perdido", "Usuario");
        for (BajaInventarioResponse b : r.getBajas()) {
            t.fila(b.getFechaHora() != null ? b.getFechaHora().format(FECHA_HORA) : "",
                    b.getNumeroLote(),
                    b.getMedicamentoCodigo() + " - " + b.getMedicamentoNombre(),
                    b.getMotivo() != null ? b.getMotivo().name() : "",
                    String.valueOf(b.getCantidad()),
                    b.getValorPerdida() != null ? b.getValorPerdida().toPlainString() : "0.00",
                    b.getUsuarioNombre());
        }
        t.totales("TOTAL", "", "", "",
                String.valueOf(r.getTotalUnidades()), "", "");
        return t;
    }
    
    private Tabla ordenes(LocalDate desde, LocalDate hasta) {
        ReporteOrdenesResponse r = reporteService.reporteOrdenes(desde, hasta);
        Tabla t = new Tabla("Ordenes de compra",
                "Periodo: " + desde.format(FECHA) + " al " + hasta.format(FECHA));
        t.encabezados("Fecha", "Proveedor", "Estado", "Solicitante", "Aprobado por", "Items", "Unidades");
        for (OrdenCompraResponse o : r.getOrdenes()) {
            t.fila(o.getFechaHora() != null ? o.getFechaHora().format(FECHA_HORA) : "",
                    o.getProveedorRazonSocial(),
                    o.getEstado() != null ? o.getEstado().name() : "",
                    o.getUsuarioNombre(),
                    o.getAprobadoPorNombre(),
                    String.valueOf(o.getTotalItems()),
                    String.valueOf(o.getTotalUnidades()));
        }
        t.totales("TOTAL", "", "", "", "",
                String.valueOf(r.getTotalOrdenes()), String.valueOf(r.getTotalUnidades()));
        return t;
    }
    
    // ------------------------------------------------------------------
    //  PDF
    // ------------------------------------------------------------------
    
    private byte[] renderizarPdf(Tabla tabla, String usuario, String rol) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(salida);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4.rotate());
            doc.setMargins(28, 28, 40, 30);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont negrita = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            doc.setFont(normal);
            
            doc.add(new Paragraph("FarmaMed - " + tabla.titulo).setFont(negrita).setFontSize(14)
                    .setFontColor(AZUL));
            doc.add(new Paragraph(tabla.subtitulo).setFontSize(9).setFontColor(ColorConstants.GRAY));
            doc.add(new Paragraph("Generado: " + LocalDateTime.now().format(FECHA_HORA)
                    + " | Usuario: " + usuario + (rol != null ? " (" + rol + ")" : ""))
                    .setFontSize(9).setFontColor(ColorConstants.GRAY));
            doc.add(new Paragraph(" "));
            
            Table tablaPdf = new Table(UnitValue.createPercentArray(tabla.encabezados.size()))
                    .useAllAvailableWidth()
                    .setFixedLayout();
            for (String h : tabla.encabezados) {
                com.itextpdf.layout.element.Cell celda =
                        new com.itextpdf.layout.element.Cell().add(new Paragraph(h).setFont(negrita).setFontSize(8));
                celda.setBackgroundColor(AZUL);
                celda.setFontColor(ColorConstants.WHITE);
                celda.setPadding(4);
                tablaPdf.addHeaderCell(celda);
            }
            for (List<String> fila : tabla.filas) {
                for (String valor : fila) {
                    com.itextpdf.layout.element.Cell celda =
                            new com.itextpdf.layout.element.Cell()
                                    .add(new Paragraph(valor != null ? valor : "").setFontSize(8));
                    celda.setPadding(3);
                    tablaPdf.addCell(celda);
                }
            }
            if (tabla.totales != null) {
                for (String valor : tabla.totales) {
                    com.itextpdf.layout.element.Cell celda =
                            new com.itextpdf.layout.element.Cell()
                                    .add(new Paragraph(valor != null ? valor : "").setFont(negrita).setFontSize(8));
                    celda.setBackgroundColor(AZUL_CLARO);
                    celda.setPadding(4);
                    tablaPdf.addCell(celda);
                }
            }
            doc.add(tablaPdf);
            doc.close();
            writer.close();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte", e);
        }
        return salida.toByteArray();
    }
    
    // ------------------------------------------------------------------
    //  XLSX
    // ------------------------------------------------------------------
    
    private byte[] renderizarXlsx(Tabla tabla, String usuario, String rol) {
        try (Workbook libro = new XSSFWorkbook()) {
            String nombreHoja = tabla.titulo.length() > 28 ? tabla.titulo.substring(0, 28) : tabla.titulo;
            Sheet hoja = libro.createSheet(nombreHoja);
            int filaActual = 0;
            
            org.apache.poi.ss.usermodel.CellStyle estiloTitulo = libro.createCellStyle();
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 13);
            fuenteTitulo.setColor(IndexedColors.DARK_BLUE.getIndex());
            estiloTitulo.setFont(fuenteTitulo);
            Row filaTitulo = hoja.createRow(filaActual++);
            org.apache.poi.ss.usermodel.Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("FarmaMed - " + tabla.titulo);
            celdaTitulo.setCellStyle(estiloTitulo);
            
            org.apache.poi.ss.usermodel.CellStyle estiloSecundario = libro.createCellStyle();
            Font fuenteSecundaria = libro.createFont();
            fuenteSecundaria.setFontHeightInPoints((short) 9);
            estiloSecundario.setFont(fuenteSecundaria);
            Row filaSubtitulo = hoja.createRow(filaActual++);
            org.apache.poi.ss.usermodel.Cell celdaSubtitulo = filaSubtitulo.createCell(0);
            celdaSubtitulo.setCellValue(tabla.subtitulo);
            celdaSubtitulo.setCellStyle(estiloSecundario);
            Row filaGenerado = hoja.createRow(filaActual++);
            org.apache.poi.ss.usermodel.Cell celdaGenerado = filaGenerado.createCell(0);
            celdaGenerado.setCellValue("Generado: " + LocalDateTime.now().format(FECHA_HORA)
                    + " | Usuario: " + usuario + (rol != null ? " (" + rol + ")" : ""));
            celdaGenerado.setCellStyle(estiloSecundario);
            filaActual++;
            
            org.apache.poi.ss.usermodel.CellStyle estiloCabecera = libro.createCellStyle();
            Font fuenteCabecera = libro.createFont();
            fuenteCabecera.setBold(true);
            fuenteCabecera.setColor(IndexedColors.WHITE.getIndex());
            estiloCabecera.setFont(fuenteCabecera);
            estiloCabecera.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloCabecera.setBorderBottom(BorderStyle.THIN);
            
            int filaCabecera = filaActual++;
            Row filaEncabezados = hoja.createRow(filaCabecera);
            for (int i = 0; i < tabla.encabezados.size(); i++) {
                org.apache.poi.ss.usermodel.Cell celda = filaEncabezados.createCell(i);
                celda.setCellValue(tabla.encabezados.get(i));
                celda.setCellStyle(estiloCabecera);
            }
            
            org.apache.poi.ss.usermodel.CellStyle estiloDato = libro.createCellStyle();
            estiloDato.setBorderBottom(BorderStyle.HAIR);
            
            for (List<String> fila : tabla.filas) {
                Row filaDatos = hoja.createRow(filaActual++);
                for (int i = 0; i < fila.size(); i++) {
                    org.apache.poi.ss.usermodel.Cell celda = filaDatos.createCell(i);
                    celda.setCellValue(fila.get(i) != null ? fila.get(i) : "");
                    celda.setCellStyle(estiloDato);
                }
            }
            
            if (tabla.totales != null) {
                org.apache.poi.ss.usermodel.CellStyle estiloTotal = libro.createCellStyle();
                Font fuenteTotal = libro.createFont();
                fuenteTotal.setBold(true);
                estiloTotal.setFont(fuenteTotal);
                estiloTotal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                estiloTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Row filaTotal = hoja.createRow(filaActual++);
                for (int i = 0; i < tabla.totales.size(); i++) {
                    org.apache.poi.ss.usermodel.Cell celda = filaTotal.createCell(i);
                    celda.setCellValue(tabla.totales.get(i) != null ? tabla.totales.get(i) : "");
                    celda.setCellStyle(estiloTotal);
                }
            }
            
            for (int i = 0; i < tabla.encabezados.size(); i++) {
                hoja.autoSizeColumn(i);
                hoja.setColumnWidth(i, Math.min(hoja.getColumnWidth(i) + 800, 12000));
            }
            hoja.createFreezePane(0, filaCabecera + 1);
            
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            libro.write(salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el XLSX del reporte", e);
        }
    }
    
    // ------------------------------------------------------------------
    //  Modelo intermedio
    // ------------------------------------------------------------------
    
    /**
     * Tabla simple: titulo, subtitulo, encabezados, filas y fila de totales.
     */
    private static final class Tabla {
        private final String titulo;
        private final String subtitulo;
        private final List<String> encabezados = new ArrayList<>();
        private final List<List<String>> filas = new ArrayList<>();
        private List<String> totales;
        
        private Tabla(String titulo, String subtitulo) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
        }
        
        private void encabezados(String... valores) {
            encabezados.addAll(Arrays.asList(valores));
        }
        
        private void fila(String... valores) {
            filas.add(Arrays.asList(valores));
        }
        
        private void totales(String... valores) {
            this.totales = Arrays.asList(valores);
        }
    }
}
