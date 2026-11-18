package com.abyssredemption.abstool.client.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Writes local SpreadsheetML workbooks with typed cells. */
public final class ExcelWorkbook {
    private static final String SHEET = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String PKG = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.";
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final DateTimeFormatter FILENAME_TIME = DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss");

    public record Table(String name, List<String> headers, List<List<?>> rows) {}

    private ExcelWorkbook() {}

    public static Path save(Path directory, String prefix, List<Table> tables) {
        Path pending = null;
        try {
            Files.createDirectories(directory);
            pending = Files.createTempFile(directory, ".abstool-", ".tmp");
            write(pending, tables);
            Path target = directory.resolve(prefix + "-" + LocalDateTime.now().format(FILENAME_TIME)
                    + "-" + UUID.randomUUID().toString().substring(0, 8) + ".xlsx");
            return Files.move(pending, target);
        } catch (IOException error) {
            if (pending != null) {
                try { Files.deleteIfExists(pending); }
                catch (IOException cleanupError) { error.addSuppressed(cleanupError); }
            }
            throw new UncheckedIOException(error);
        }
    }

    static void write(Path path, List<Table> tables) throws IOException {
        try (var zip = new ZipOutputStream(Files.newOutputStream(path))) {
            var out = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
            var types = new StringBuilder("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
                    .append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
                    .append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
                    .append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"" + MIME + "sheet.main+xml\"/>");
            var workbook = new StringBuilder("<workbook xmlns=\"" + SHEET + "\" xmlns:r=\"" + REL + "\">"
                    + "<bookViews><workbookView/></bookViews><sheets>");
            var relations = new StringBuilder("<Relationships xmlns=\"" + PKG + "\">");
            for (int i = 1; i <= tables.size(); i++) {
                String target = "worksheets/sheet" + i + ".xml";
                types.append("<Override PartName=\"/xl/").append(target)
                        .append("\" ContentType=\"" + MIME + "worksheet+xml\"/>");
                workbook.append("<sheet name=\"").append(escape(tables.get(i - 1).name()))
                        .append("\" sheetId=\"").append(i).append("\" r:id=\"rId").append(i).append("\"/>");
                relations.append(relationship(i, "worksheet", target));
                begin(zip, out, "xl/" + target);
                writeTable(out, tables.get(i - 1));
                end(zip, out);
            }
            part(zip, out, "[Content_Types].xml", types.append("</Types>").toString());
            part(zip, out, "_rels/.rels", "<Relationships xmlns=\"" + PKG + "\">"
                    + relationship(1, "officeDocument", "xl/workbook.xml") + "</Relationships>");
            part(zip, out, "xl/workbook.xml", workbook.append("</sheets></workbook>").toString());
            part(zip, out, "xl/_rels/workbook.xml.rels", relations.append("</Relationships>").toString());
        }
    }

    private static String relationship(int id, String type, String target) {
        return "<Relationship Id=\"rId" + id + "\" Type=\"" + REL + "/" + type + "\" Target=\"" + target + "\"/>";
    }

    private static void writeTable(BufferedWriter out, Table table) throws IOException {
        out.write("<worksheet xmlns=\"" + SHEET + "\"><sheetViews><sheetView workbookViewId=\"0\">"
                + "<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>"
                + "</sheetView></sheetViews><sheetFormatPr defaultColWidth=\"22\" defaultRowHeight=\"15\"/><sheetData>");
        writeRow(out, 1, table.headers());
        for (int i = 0; i < table.rows().size(); i++) writeRow(out, i + 2, table.rows().get(i));
        out.write("</sheetData>");
        if (!table.rows().isEmpty()) {
            // Statistics sheets use at most 26 columns.
            char column = (char) ('A' + table.headers().size() - 1);
            out.write("<autoFilter ref=\"A1:" + column + (table.rows().size() + 1) + "\"/>");
        }
        out.write("</worksheet>");
    }

    private static void writeRow(BufferedWriter out, int row, List<?> values) throws IOException {
        out.write("<row r=\"" + row + "\">");
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            out.write("<c r=\"" + (char) ('A' + i) + row + "\"");
            if (value instanceof Boolean flag) {
                out.write(flag ? " t=\"b\"><v>1</v></c>" : " t=\"b\"><v>0</v></c>");
            } else if (value instanceof Number number) {
                out.write(" t=\"n\"><v>" + number + "</v></c>");
            } else {
                // Inline text keeps names beginning with '=' from being interpreted as formulas.
                out.write(" t=\"inlineStr\"><is><t xml:space=\"preserve\">" + escape(value.toString()) + "</t></is></c>");
            }
        }
        out.write("</row>");
    }

    private static String escape(String input) {
        var result = new StringBuilder(input.length());
        input.codePoints().forEach(cp -> {
            switch (cp) {
                case '&' -> result.append("&amp;");
                case '<' -> result.append("&lt;");
                case '>' -> result.append("&gt;");
                case '"' -> result.append("&quot;");
                case 9, 10, 13 -> result.append("&#").append(cp).append(';');
                default -> {
                    if (cp >= 32 && cp <= 0xD7FF || cp >= 0xE000 && cp <= 0xFFFD || cp >= 0x10000) result.appendCodePoint(cp);
                }
            }
        });
        return result.toString();
    }

    private static void part(ZipOutputStream zip, BufferedWriter out, String name, String text) throws IOException {
        begin(zip, out, name);
        out.write(text);
        end(zip, out);
    }

    private static void begin(ZipOutputStream zip, BufferedWriter out, String name) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        out.write(XML);
    }

    private static void end(ZipOutputStream zip, BufferedWriter out) throws IOException {
        out.flush();
        zip.closeEntry();
    }
}
