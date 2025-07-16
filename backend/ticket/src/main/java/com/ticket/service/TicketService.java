
package com.ticket.service;

import com.ticket.entity.Ticket;
import com.ticket.exception.TicketNotFoundException;
import com.ticket.repository.TicketRepository;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket saveTicket(Ticket ticket) {
        try {
            return ticketRepository.save(ticket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save ticket: " + e.getMessage(), e);
        }
    }

    public List<Ticket> getAllTickets() {
        try {
            return ticketRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve tickets: " + e.getMessage(), e);
        }
    }

    public Optional<Ticket> getTicketById(Long id) {
        try {
            return ticketRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve ticket: " + e.getMessage(), e);
        }
    }

    public Ticket updateTicket(Long id, Ticket updatedTicket) {
        try {
            return ticketRepository.findById(id)
                    .map(existingTicket -> {
                        existingTicket.setTitleTicket(updatedTicket.getTitleTicket());
                        existingTicket.setAssign(updatedTicket.getAssign());
                        existingTicket.setStatus(updatedTicket.getStatus());
                        existingTicket.setPriority(updatedTicket.getPriority());
                        if (updatedTicket.getDateCurrent() != null) {
                            existingTicket.setDateCurrent(updatedTicket.getDateCurrent());
                        }
                        existingTicket.setDescription(updatedTicket.getDescription());
                        existingTicket.setPdfData(updatedTicket.getPdfData());
                        existingTicket.setDocxData(updatedTicket.getDocxData());

                        return ticketRepository.save(existingTicket);
                    })
                    .orElseThrow(() -> new TicketNotFoundException(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ticket: " + e.getMessage(), e);
        }
    }

    public Ticket updateTicketDescription(Long id, String description) {
        return ticketRepository.findById(id)
                .map(ticket -> {
                    ticket.setDescription(description);

                    // --- HTML to PDF/DOCX Conversion Logic ---
                    try {
                        byte[] pdfBytes = convertHtmlToPdf(description);
                        ticket.setPdfData(pdfBytes);
                    } catch (Exception e) {
                        System.err.println("Failed to convert HTML to PDF for ticket " + id + ": " + e.getMessage());
                        e.printStackTrace();
                        ticket.setPdfData(null);
                    }

                    try {
                        byte[] docxBytes = convertHtmlToDocx(description);
                        ticket.setDocxData(docxBytes);
                    } catch (Exception e) {
                        System.err.println("Failed to convert HTML to DOCX for ticket " + id + ": " + e.getMessage());
                        e.printStackTrace();
                        ticket.setDocxData(null);
                    }
                    // --------------------------------------------------------

                    return ticketRepository.save(ticket);
                })
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    // HTML to PDF conversion using OpenHTMLToPDF
    private byte[] convertHtmlToPdf(String htmlContent) throws Exception {
        // Use Jsoup to parse and clean the HTML, converting named entities to numeric
        // This helps the XML parser handle entities like &nbsp;
        Document doc = Jsoup.parse(htmlContent);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // Output as XML syntax
        doc.outputSettings().charset("UTF-8");
        // Ensure that HTML entities are escaped to numeric entities for XML parsers
        doc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml); 
        String cleanedHtml = doc.html();

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            // Pass the cleaned HTML to the builder
            builder.withHtmlContent(cleanedHtml, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }

    // HTML to DOCX conversion using Apache POI and Jsoup (simplified)
    private byte[] convertHtmlToDocx(String htmlContent) throws Exception {
        // Use Jsoup to parse and clean the HTML for DOCX conversion as well
        // This ensures consistent handling of entities and structure
        Document htmlDoc = Jsoup.parse(htmlContent);
        // For DOCX, you might want browser-like HTML output or just the text,
        // but Jsoup.parse() already cleans it up well.
        // You can convert named entities to numeric here too if needed, but POI might be more lenient.
        // htmlDoc.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
        // String cleanedHtml = htmlDoc.html(); // Use this if you need to pass processed HTML to the loop

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // Iterate over elements from the Jsoup parsed document's body
            for (Element element : htmlDoc.body().children()) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();

                // Basic handling for common tags
                if (element.tagName().equals("p")) {
                    run.setText(element.text());
                } else if (element.tagName().equals("h1")) {
                    run.setText(element.text());
                    run.setBold(true);
                    run.setFontSize(24);
                } else if (element.tagName().equals("h2")) {
                    run.setText(element.text());
                    run.setBold(true);
                    run.setFontSize(18);
                } else if (element.tagName().equals("h3")) {
                    run.setText(element.text());
                    run.setBold(true);
                    run.setFontSize(14);
                } else if (element.tagName().equals("strong") || element.tagName().equals("b")) {
                    run.setText(element.text());
                    run.setBold(true);
                } else if (element.tagName().equals("em") || element.tagName().equals("i")) {
                    run.setText(element.text());
                    run.setItalic(true);
                } else if (element.tagName().equals("ul") || element.tagName().equals("ol")) {
                    for (Element li : element.children()) {
                        XWPFParagraph listItemParagraph = document.createParagraph();
                        XWPFRun listItemRun = listItemParagraph.createRun();
                        if (element.tagName().equals("ul")) {
                            listItemRun.setText("• " + li.text());
                        } else {
                            listItemRun.setText(li.text());
                        }
                    }
                }
                else {
                    run.setText(element.text());
                }
            }

            document.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] getPdfDataById(Long id) {
        return ticketRepository.findById(id)
                .map(Ticket::getPdfData)
                .orElseThrow(() -> new TicketNotFoundException("PDF data not found for ticket id: " + id));
    }

    public byte[] getDocxDataById(Long id) {
        return ticketRepository.findById(id)
                .map(Ticket::getDocxData)
                .orElseThrow(() -> new TicketNotFoundException("DOCX data not found for ticket id: " + id));
    }

    public void deleteTicket(Long id) {
        try {
            if (!ticketRepository.existsById(id)) {
                throw new TicketNotFoundException(id);
            }
            ticketRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ticket: " + e.getMessage(), e);
        }
    }
}