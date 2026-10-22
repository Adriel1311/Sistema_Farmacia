---
name: Clinical Material Pharmaceutical System
colors:
  surface: '#fbf9f9'
  surface-dim: '#dbdad9'
  surface-bright: '#fbf9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f3'
  surface-container: '#efeded'
  surface-container-high: '#e9e8e7'
  surface-container-highest: '#e3e2e2'
  on-surface: '#1b1c1c'
  on-surface-variant: '#414752'
  inverse-surface: '#303031'
  inverse-on-surface: '#f2f0f0'
  outline: '#717783'
  outline-variant: '#c1c6d4'
  surface-tint: '#005faf'
  primary: '#005dac'
  on-primary: '#ffffff'
  primary-container: '#1976d2'
  on-primary-container: '#fffdff'
  inverse-primary: '#a5c8ff'
  secondary: '#2b5bb5'
  on-secondary: '#ffffff'
  secondary-container: '#759efd'
  on-secondary-container: '#00337c'
  tertiary: '#006771'
  on-tertiary: '#ffffff'
  tertiary-container: '#00828e'
  on-tertiary-container: '#fcffff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d4e3ff'
  primary-fixed-dim: '#a5c8ff'
  on-primary-fixed: '#001c3a'
  on-primary-fixed-variant: '#004786'
  secondary-fixed: '#d9e2ff'
  secondary-fixed-dim: '#b0c6ff'
  on-secondary-fixed: '#001945'
  on-secondary-fixed-variant: '#00429c'
  tertiary-fixed: '#92f1fe'
  tertiary-fixed-dim: '#75d5e2'
  on-tertiary-fixed: '#001f23'
  on-tertiary-fixed-variant: '#004f56'
  background: '#fbf9f9'
  on-background: '#1b1c1c'
  surface-variant: '#e3e2e2'
typography:
  headline-lg:
    fontFamily: Roboto Flex
    fontSize: 32px
    fontWeight: '400'
    lineHeight: 40px
  headline-md:
    fontFamily: Roboto Flex
    fontSize: 24px
    fontWeight: '500'
    lineHeight: 32px
  headline-sm:
    fontFamily: Roboto Flex
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
  title-md:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  title-sm:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  body-lg:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  body-sm:
    fontFamily: Roboto Flex
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  label-lg:
    fontFamily: Roboto Flex
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Roboto Flex
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  label-sm:
    fontFamily: Roboto Flex
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.5px
  code-batch:
    fontFamily: Roboto Flex
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 1px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.5rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style
The design system establishes a clinical, precise, and authoritative visual environment designed specifically for hospital pharmacy management, drug tracking, batch expiration monitoring, and unit-dose dispensing.

The brand persona is governed by three imperatives: absolute data clarity, procedural trust, and zero-error ergonomics. Pharmacists, pharmacy technicians, and nursing staff operate under intense cognitive load; therefore, the visual environment rejects gratuitous ornamentation in favor of strict, systematic Material Design discipline. 

The emotional response sought is calm control, institutional reliability, and immediate situational awareness. High visual contrast between status chips and neutral surfaces ensures critical patient-safety signals (such as expired or nearing-expiration pharmaceuticals) are perceived without latency.

## Colors
The color architecture relies on high-trust medical blues, disciplined functional neutrals, and a strict multi-tier triage palette for drug expiration governance.

### Base Structural Palette
- **Application Canvas**: `#F5F5F5` (Material Grey 100), provides gentle visual relaxation for prolonged station shifts.
- **Card & Surface Background**: `#FFFFFF` (Pure White), providing crisp boundaries against `#F5F5F5`.
- **Primary Brand / Action**: `#1976D2` (Material Blue 700), utilized for actionable controls, primary navigation anchors, and focal indicators.
- **Primary Deep / Contrast**: `#0D47A1` (Material Blue 900), applied to the main clinical toolbar, key headers, and selected active-state drawer navigation.
- **Neutral Foreground**: `#212121` (High Emphasis Text, 87% equivalent on light), `#616161` (Medium Emphasis Text, 60%), and `#9E9E9E` (Disabled Text).
- **Divider & Border Lines**: `#E0E0E0` (Material Grey 300), 1px solid definitions for data tables and list splits.

### Drug Expiry Status Palette (Critical Semáforo)
These status colors are non-negotiable semantic tokens engineered for accessibility and immediate differentiation across all inventory cards and table rows:
- **Vigente (> 60 days)**: Foreground `#2E7D32` on Background `#E8F5E9` (Material Green 800 on Green 50). Signifies stable stock.
- **Próximo a Vencer (31–60 days)**: Foreground `#F57C00` on Background `#FFF3E0` (Material Orange 700 on Orange 50). Alerts dispatch optimization.
- **Crítico / Inminente (0–30 days)**: Foreground `#D32F2F` on Background `#FFEBEE` (Material Red 700 on Red 50). Demands immediate segregation or quarantine.
- **Vencido (≤ 0 days)**: Solid Background `#212121` with `#FFFFFF` text (or outlined `#212121` on `#EEEEEE` for secondary tags). Signifies total prohibition of dispensing.

## Typography
The system enforces Roboto Flex across all interfaces to guarantee cross-platform legibility, precise tabular data alignment, and recognizable clinical structure.

- **Numerals & Batch Codes**: Numeric values within inventory counts, dosage calculations, and national pharmaceutical catalog numbers require tabular layout. For serial and lot numbers, the dedicated `code-batch` style enforces letter spacing to minimize transcription errors between similar characters (e.g., `0` vs `O`, `1` vs `I`).
- **Hierarchy Rules**: 
  - `headline-lg` is restricted to hospital department overview dashboards and system settings.
  - `headline-sm` anchors workstation views and master-detail headers.
  - `title-md` and `title-sm` are deployed across card titles, table column headers, and modal dialog heads.
  - `label-md` and `label-sm` govern table data pills, chip labels, barcode captions, and helper annotations.

## Layout & Spacing
The layout follows a strict 8px-baseline grid system (with 4px sub-increments for compact clinical data entry).

### Layout Structure
- **Persistent Shell**: Built around the standard Angular Material application scaffold:
  - Top Toolbar (`mat-toolbar`): Fixed 64px height (`space-xl` * 2) desktop; 56px mobile.
  - Collapsible Navigation Drawer (`mat-sidenav`): 260px fixed width, sliding over on mobile/tablet portrait, pinned side-by-side on wide screens.
  - Content Workspace: Fluid container stretching between 1024px and 1920px with a default margin of `1.5rem` (`margin`).
- **Data Densities**:
  - **Standard Mode**: Applied across ward dashboards and general management pages (`space-md` cell padding).
  - **High-Density Mode**: Applied to high-volume pharmacy dispensing logs and inventory batch lookup tables, reducing vertical cell paddings to `space-xs` (4px top/bottom) and horizontal padding to `space-sm` (8px).

### Breakpoints
- **Desktop (≥ 1280px)**: 12-column grid, 16px gutter (`gutter`), 24px margins (`margin`). Master-detail layouts sit side-by-side.
- **Tablet Landscape (960px - 1279px)**: 8-column grid, 16px gutter, collapsible navigation drawer.
- **Tablet Portrait & Mobile (< 960px)**: 4-column grid, 8px gutter (`gutter-mobile`), 16px outer margin (`margin-mobile`). Data tables degrade gracefully into vertical summary cards.

## Elevation & Depth
Elevation communicates hierarchy, containment, and interactive priority using classic Material Design multi-directional shadows. Shadows use a neutral grey tone (`#000000`) at low opacity levels to maintain clinical cleanliness without creating dirty edges against the `#F5F5F5` canvas.

### Elevation Levels
- **Elevation 0 (Flat)**:
  - Used for raw application canvas (`#F5F5F5`), table rows at rest, dividers, and disabled cards.
  - Shadow: `none`.
- **Elevation 1 (Resting Surface)**:
  - The standard resting state for `mat-card`, summary panels, and input container groups.
  - Shadow: `0px 2px 1px -1px rgba(0,0,0,0.06), 0px 1px 1px 0px rgba(0,0,0,0.04), 0px 1px 3px 0px rgba(0,0,0,0.08)`.
- **Elevation 2 (Interactive Hover & Elevated State)**:
  - Applied to cards on hover, active batch items during selection, and raised buttons (`mat-raised-button`).
  - Shadow: `0px 3px 3px -2px rgba(0,0,0,0.08), 0px 3px 4px 0px rgba(0,0,0,0.05), 0px 1px 8px 0px rgba(0,0,0,0.08)`.
- **Elevation 3 (Top Bars & Navigation Rails)**:
  - Applied to `mat-toolbar` when scrolling content passes underneath, sticky table headers, and drop-down menu overlays.
  - Shadow: `0px 3px 5px -1px rgba(0,0,0,0.1), 0px 6px 10px 0px rgba(0,0,0,0.07), 0px 1px 18px 0px rgba(0,0,0,0.06)`.
- **Elevation 4 (Modals, Dialogs, Emergency Prescriptions)**:
  - Applied to confirmation popups, urgent prescription verification modals (`mat-dialog`), and critical alerts.
  - Shadow: `0px 5px 5px -3px rgba(0,0,0,0.12), 0px 8px 10px 1px rgba(0,0,0,0.08), 0px 3px 14px 2px rgba(0,0,0,0.06)`.

## Shapes
To preserve an orderly, institutional feel aligned with classic Angular Material standards, the system relies on soft, restrained corner geometry (`roundedness: 1`).

- **Cards & Surfaces (`mat-card`)**: 4px border radius (`0.25rem`). This provides clean alignment in modular multi-card dashboards.
- **Buttons (`mat-button`, `mat-raised-button`, `mat-flat-button`)**: 4px border radius (`0.25rem`), keeping them recognizable and structured.
- **Form Controls (`mat-form-field`)**: 4px top border radius with 0px bottom corners when using the filled style, or uniform 4px corner radius for outlined variants.
- **Status Badges & Chips (`mat-chip`)**: 16px border radius (full pill) to visually distinguish meta-information and state chips from clickable rectangular buttons.
- **Dialogs & Overlays (`mat-dialog`)**: 4px or 8px max radius (`0.5rem`) to frame medical warnings without appearing overly playful.

## Components

### Buttons (`mat-button`, `mat-raised-button`, `mat-flat-button`, `mat-stroked-button`)
- **Primary Action (Dispense, Register)**: `mat-raised-button` with `#1976D2` background, `#FFFFFF` text, 4px border-radius, uppercase text transform, 36px standard height, elevation-1 resting, elevation-2 on hover.
- **Secondary Action (Cancel, Export)**: `mat-stroked-button` with a 1px solid `#E0E0E0` border, `#1976D2` or `#616161` label, transparent background.
- **Dangerous Action (Quarantine, Discard Expired Lot)**: `mat-flat-button` with `#D32F2F` background, `#FFFFFF` text.

### Expiry Chips & Status Indicators (`mat-chip`)
Chips are 24px in height with a 12px horizontal padding and a strict 16px radius:
- **Vigente (> 60 días)**: Background `#E8F5E9`, text `#2E7D32`, icon `check_circle` (16px).
- **Próximo a Vencer (31–60 días)**: Background `#FFF3E0`, text `#F57C00`, icon `warning` (16px).
- **Crítico (0–30 días)**: Background `#FFEBEE`, text `#D32F2F`, icon `error` (16px).
- **Vencido (≤ 0 días)**: Background `#212121`, text `#FFFFFF`, icon `block` (16px), font-weight 600.

### Data Tables (`mat-table`)
- **Header**: `#FFFFFF` or `#FAFAFA` background, 56px height, text in `#616161`, uppercase 12px bold, 1px solid bottom border (`#E0E0E0`).
- **Rows**: 48px height in standard mode (36px in compact dispensary mode), hover state turns background to `#F5F5F5`. Bottom border 1px solid `#F0F0F0`.
- **Row Critical Tint**: Rows containing expired drugs highlight with a subtle `#FFEBEE` 20% opacity wash and a 4px solid `#D32F2F` border on the far left edge.

### Cards (`mat-card`)
- Flat white surface `#FFFFFF`, elevation-1, 16px internal padding.
- Card Header utilizes `mat-card-title` in `title-md` weight 500, with optional metadata in `mat-card-subtitle` (`#757575`).
- Divider lines (`mat-divider`) inside cards are set to `#EEEEEE`.

### Form Fields (`mat-form-field`)
- Outlined variation is prioritized for medication intake workflows.
- Border default is `#BDBDBD`, changing to `#1976D2` on active focus with a 2px outline width.
- Error state outlines switch immediately to `#D32F2F` with inline validation text in `body-sm`.
- Suffix icons (`mat-icon`) serve barcode scanner hardware triggers or quick-clear actions.

### Checkboxes & Radios (`mat-checkbox`, `mat-radio-button`)
- Active color transitions to `#1976D2`.
- Checkbox square border: 2px solid `#757575` when unchecked; filled with `#1976D2` with a white checkmark when selected.

### Navigation & Top Shelf (`mat-toolbar` & `mat-sidenav`)
- Main horizontal application bar (`mat-toolbar`) rendered in Deep Primary `#0D47A1` with crisp white typography and icons.
- Sidenav drawer holds a white background `#FFFFFF`, separated by a 1px border `#E0E0E0`. Active navigation items display a left 4px solid indicator of `#1976D2` and a `#E3F2FD` background tint.