package com.medicalstore.pos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.oned.Code128Writer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Genera las imagenes de codigo de barras (CODE 128) y codigo QR que se
 * imprimen en las etiquetas de medicamentos y lotes, y en el comprobante de
 * entrega (ticket TI-XXXXXX).
 *
 * Se usa ZXing solo para codificar: la matriz de bits se dibuja a mano en un
 * BufferedImage, asi que no hace falta la libreria grafica "javase".
 */
@Service
public class CodigoBarrasService {
    
    private static final int ANCHO = 400;
    private static final int ALTO_CODIGO = 140;
    private static final int ALTO_QR = 400;
    private static final int MARGEN = 10;
    
    /**
     * Codigo de barras CODE 128 con el valor dado.
     *
     * @return PNG del codigo de barras.
     */
    public byte[] codigoBarras(String valor) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 4);
            BitMatrix matriz = new Code128Writer().encode(valor, BarcodeFormat.CODE_128, ANCHO, ALTO_CODIGO, hints);
            return png(matriz, ANCHO, ALTO_CODIGO);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el codigo de barras", e);
        }
    }
    
    /**
     * Codigo QR con el valor dado (por ejemplo, el contenido del comprobante
     * de entrega: numero de ticket, paciente y medicamentos).
     *
     * @return PNG del codigo QR.
     */
    public byte[] codigoQr(String valor) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix matriz = new QRCodeWriter().encode(valor, BarcodeFormat.QR_CODE, ALTO_QR, ALTO_QR, hints);
            return png(matriz, ALTO_QR, ALTO_QR);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el codigo QR", e);
        }
    }
    
    /**
     * Dibuja la matriz de bits y la devuelve como PNG en blanco y negro.
     */
    private byte[] png(BitMatrix matriz, int ancho, int alto) {
        BufferedImage imagen = new BufferedImage(ancho + MARGEN * 2, alto + MARGEN * 2, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < imagen.getWidth(); x++) {
            for (int y = 0; y < imagen.getHeight(); y++) {
                int px = x - MARGEN;
                int py = y - MARGEN;
                boolean negro = px >= 0 && py >= 0 && px < ancho && py < alto && matriz.get(px, py);
                imagen.setRGB(x, y, negro ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo escribir la imagen PNG", e);
        }
    }
}
