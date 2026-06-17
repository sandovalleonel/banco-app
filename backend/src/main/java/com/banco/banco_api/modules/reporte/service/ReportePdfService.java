package com.banco.banco_api.modules.reporte.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.movimiento.domain.MovimientoEntity;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportePdfService {

    public String generatePdfBase64(ClienteEntity cliente, List<CuentaEntity> cuentas, 
                                     List<MovimientoEntity> movimientos, 
                                     LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Colors for professional clean styling
            Color primaryColor = new Color(30, 58, 138); // Deep Blue
            Color secondaryColor = new Color(75, 85, 99); // Slate Gray
            Color tableHeaderBg = new Color(59, 130, 246); // Blue accent

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, primaryColor);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, primaryColor);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, secondaryColor);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, Color.BLACK);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, secondaryColor);

            // Title
            Paragraph title = new Paragraph("ESTADO DE CUENTA BANCARIO", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Metadata & Client Info Table
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaGeneracion = LocalDateTime.now().format(dtf);
            String rangoFechas = startDate.toString() + " a " + endDate.toString();

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20f);

            addInfoCell(infoTable, "Cliente:", cliente.getNombre(), labelFont, valueFont);
            addInfoCell(infoTable, "Identificación:", cliente.getIdentificacion(), labelFont, valueFont);
            addInfoCell(infoTable, "Fecha de Generación:", fechaGeneracion, labelFont, valueFont);
            addInfoCell(infoTable, "Rango de Búsqueda:", rangoFechas, labelFont, valueFont);

            document.add(infoTable);

            // Accounts and Movements
            if (cuentas == null || cuentas.isEmpty()) {
                Paragraph noCuentas = new Paragraph("El cliente no tiene cuentas", sectionFont);
                noCuentas.setAlignment(Paragraph.ALIGN_CENTER);
                noCuentas.setSpacingBefore(30f);
                document.add(noCuentas);
            } else {
                for (CuentaEntity cuenta : cuentas) {
                    Paragraph sectionHeader = new Paragraph("Estado de Cuenta - N° " + cuenta.getNumeroCuenta(), sectionFont);
                    sectionHeader.setSpacingBefore(15f);
                    sectionHeader.setSpacingAfter(5f);
                    document.add(sectionHeader);

                    // Cuenta details summary table
                    PdfPTable cuentaInfoTable = new PdfPTable(5);
                    cuentaInfoTable.setWidthPercentage(100);
                    cuentaInfoTable.setSpacingAfter(10f);

                    // Headers
                    PdfPCell ch1 = new PdfPCell(new Paragraph("Tipo Cuenta", labelFont));
                    PdfPCell ch2 = new PdfPCell(new Paragraph("Saldo Inicial", labelFont));
                    PdfPCell ch3 = new PdfPCell(new Paragraph("Saldo Actual", labelFont));
                    PdfPCell ch4 = new PdfPCell(new Paragraph("Estado", labelFont));
                    PdfPCell ch5 = new PdfPCell(new Paragraph("Número Cuenta", labelFont));
                    for (PdfPCell c : new PdfPCell[]{ch1, ch2, ch3, ch4, ch5}) {
                        c.setBorder(PdfPCell.BOTTOM);
                        c.setPadding(5f);
                        cuentaInfoTable.addCell(c);
                    }

                    // Values
                    PdfPCell cv1 = new PdfPCell(new Paragraph(cuenta.getTipoCuenta(), valueFont));
                    PdfPCell cv2 = new PdfPCell(new Paragraph("$" + cuenta.getSaldoInicial().toString(), valueFont));
                    PdfPCell cv3 = new PdfPCell(new Paragraph("$" + (cuenta.getSaldoActual() != null ? cuenta.getSaldoActual().toString() : "0.00"), valueFont));
                    PdfPCell cv4 = new PdfPCell(new Paragraph(cuenta.getEstado() ? "Activa" : "Inactiva", valueFont));
                    PdfPCell cv5 = new PdfPCell(new Paragraph(cuenta.getNumeroCuenta(), valueFont));
                    for (PdfPCell c : new PdfPCell[]{cv1, cv2, cv3, cv4, cv5}) {
                        c.setBorder(PdfPCell.NO_BORDER);
                        c.setPadding(5f);
                        cuentaInfoTable.addCell(c);
                    }

                    document.add(cuentaInfoTable);

                    // Filter movements for this account
                    List<MovimientoEntity> accountMovs = movimientos.stream()
                            .filter(m -> m.getCuenta().getNumeroCuenta().equals(cuenta.getNumeroCuenta()))
                            .collect(Collectors.toList());

                    if (accountMovs.isEmpty()) {
                        Paragraph noMov = new Paragraph("No se registran movimientos para esta cuenta en el rango de fechas seleccionado.", italicFont);
                        noMov.setSpacingAfter(15f);
                        document.add(noMov);
                    } else {
                        PdfPTable movTable = new PdfPTable(4);
                        movTable.setWidthPercentage(100);
                        movTable.setSpacingAfter(15f);

                        // Table Headers
                        PdfPCell h1 = new PdfPCell(new Paragraph("Fecha", headerFont));
                        PdfPCell h2 = new PdfPCell(new Paragraph("Tipo de Movimiento", headerFont));
                        PdfPCell h3 = new PdfPCell(new Paragraph("Valor", headerFont));
                        PdfPCell h4 = new PdfPCell(new Paragraph("Saldo Disponible", headerFont));

                        for (PdfPCell cell : new PdfPCell[]{h1, h2, h3, h4}) {
                            cell.setBackgroundColor(tableHeaderBg);
                            cell.setPadding(6f);
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            movTable.addCell(cell);
                        }

                        // Table Data
                        for (MovimientoEntity m : accountMovs) {
                            String fechaStr = m.getFecha().format(dtf);
                            PdfPCell d1 = new PdfPCell(new Paragraph(fechaStr, textFont));
                            PdfPCell d2 = new PdfPCell(new Paragraph(m.getTipoMovimiento(), textFont));
                            String valorFormateado = m.getValor().doubleValue() < 0
                                    ? "- $" + Math.abs(m.getValor().doubleValue())
                                    : "$" + m.getValor().toString();

                            PdfPCell d3 = new PdfPCell(new Paragraph(valorFormateado, textFont));
                            PdfPCell d4 = new PdfPCell(new Paragraph("$" + m.getSaldo().toString(), textFont));

                            for (PdfPCell cell : new PdfPCell[]{d1, d2, d3, d4}) {
                                cell.setPadding(5f);
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                                movTable.addCell(cell);
                            }
                        }
                        document.add(movTable);
                    }
                }
            }

            document.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF del reporte: " + e.getMessage(), e);
        }
    }

    private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(4f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Paragraph(value, valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(4f);
        table.addCell(valueCell);
    }
}
