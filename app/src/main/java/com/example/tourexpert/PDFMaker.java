package com.example.tourexpert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class PDFMaker extends AppCompatActivity {

    public boolean createPDFreceipt(Context source, Purchase purchase) throws IOException {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        // region create file:
        File receipt = new File("/storage/self/primary/Documents" + File.separator + purchase.getKey() + ".pdf");
        if (ContextCompat.checkSelfPermission(source, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            try {
                if (!receipt.exists()) {
                    receipt.createNewFile();
                    // get an proxy instance to the file we created:
                    Document document = new Document();
                    // save the file (its still empty)
                    PdfWriter.getInstance(document, new FileOutputStream(receipt.getPath()));
                    // open to write
                    document.open();

                    // region PDF COMMON SETTINGS:
                    // add image of company to header:
                    Bitmap bm = BitmapFactory.decodeResource(source.getResources(), R.drawable.company_logo);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    Image img = Image.getInstance(byteArray);
                    img.setAlignment(Element.ALIGN_CENTER);
                    img.scaleAbsolute(150, 100);
                    document.add(img);

                    // settings
                    document.setPageSize(PageSize.A6);
                    document.addCreationDate();
                    document.addAuthor("tourExpert");
                    document.addCreator("tourExpert application");

                    // import the custom font:
                    BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);
                    Font titleFont = new Font(fontName, 20.0f, Font.NORMAL, BaseColor.BLACK);
                    // endregion PDF SETTINGS:

                    // region COMMON RECEIPT CONTENTS:
                    // get the date of Purchase, later used in each Receipt:
                    LocalDateTime dateOfPurchase =
                            LocalDateTime.ofInstant(Instant.ofEpochMilli(purchase.getDateOfPurchase()),
                                    TimeZone.getDefault().toZoneId());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    String dateOfPurchaseAsString = dateOfPurchase.format(formatter);

                    // open a table (common between all receipt's):
                    PdfPTable table = new PdfPTable(2);

                    // set the width of the table:
                    float[] table_widths = {50f, 60f};
                    table.setTotalWidth(table_widths);

                    // add the common table contents (common between all receipt:
                    table.addCell("purchase date:");
                    table.addCell(dateOfPurchaseAsString);
                    table.addCell("amount:");
                    table.addCell(purchase.getAmount() + "");
                    table.addCell("price:");
                    table.addCell(purchase.getPrice() + "$");

                    String qrCodeText = ""; // content shown when scanning the qr code, initialized in the if { } block's

                    // endregion COMMON FILE CONTENTS:

                    if (purchase instanceof FlightPurchase) {
                        // initialize a flight object
                        FlightPurchase flightPurchase = (FlightPurchase) purchase;

                        LocalDateTime dateOfFlight =
                                LocalDateTime.ofInstant(Instant.ofEpochMilli(flightPurchase.getDateOfFlight()),
                                        TimeZone.getDefault().toZoneId());

                        String dateOfFlightAsString = dateOfFlight.format(formatter);

                        // add a title:
                        addNewItem(document, "Flight ticket purchase:", Element.ALIGN_CENTER, titleFont);

                        // add the table contents (unique to the Flight receipt:
                        table.addCell("date of flight:");
                        table.addCell(dateOfFlightAsString);
                        table.addCell("source:");
                        table.addCell(flightPurchase.getSource());
                        table.addCell("destination:");
                        table.addCell(flightPurchase.getDestination());
                        table.addCell("class:");
                        table.addCell(flightPurchase.getFlightClass());

                        // append the table to the PDF (after we construct it with unique content of a certain Receipt type):
                        document.add(table);

                        qrCodeText = "this is an official receipt for flight number: " + flightPurchase.getKey();
                    } else if (purchase instanceof AttractionPurchase) {
                        AttractionPurchase attractionPurchase = (AttractionPurchase) purchase;

                        LocalDateTime dateOfAttraction =
                                LocalDateTime.ofInstant(Instant.ofEpochMilli(attractionPurchase.getDateOfAttraction()),
                                        TimeZone.getDefault().toZoneId());

                        String dateOfAttractionAsString = dateOfAttraction.format(formatter);

                        // add a title:
                        addNewItem(document, "Attraction ticket purchase:", Element.ALIGN_CENTER, titleFont);
                        // add the table contents (unique to the attraction receipt)
                        table.addCell("date of attraction:");
                        table.addCell(dateOfAttractionAsString);
                        table.addCell("type:");
                        table.addCell(attractionPurchase.getType());

                        // append the table to the PDF (after we construct it with unique content of a certain Receipt type):
                        document.add(table);

                        addNewItem(document, "Description:", Element.ALIGN_CENTER, titleFont);
                        LineSeparator lineSeparator = new LineSeparator();
                        lineSeparator.setPercentage(40);
                        document.add(lineSeparator);
                        addNewItem(document, attractionPurchase.getDescription(), Element.ALIGN_UNDEFINED, new Font(fontName, 12.0f, Font.NORMAL, BaseColor.BLACK));

                        qrCodeText = "this is an official receipt number: " + attractionPurchase.getKey() + " for an attraction, TourExpert is associated and recognized by" +
                                " the International Association of Amusement Parks and Attractions (IAAPA) @" + dateOfAttraction.getYear();

                    } else if (purchase instanceof HotelPurchase) {
                        HotelPurchase hotelPurchase = (HotelPurchase) purchase;

                        // add a title:
                        addNewItem(document, hotelPurchase.getHotelName() + " rooms rental receipt", Element.ALIGN_CENTER, titleFont);
                        // add the table contents (unique to the attraction receipt)
                        table.addCell("Hotel: ");
                        table.addCell(hotelPurchase.getHotelName());
                        table.addCell("Room type:");
                        table.addCell(hotelPurchase.getType());
                        table.addCell("Days of rental: ");
                        table.addCell(hotelPurchase.getDays() + "");
                        table.addCell("arrival date: ");
                        table.addCell(hotelPurchase.getArrivalDate());
                        table.addCell("departure date: ");
                        table.addCell(hotelPurchase.getDepartureDate());

                        // append the table to the PDF (after we construct it with unique content of a certain Receipt type):
                        document.add(table);
                        addNewItem(document, "Description:", Element.ALIGN_CENTER, titleFont);

                        LineSeparator lineSeparator = new LineSeparator();
                        lineSeparator.setPercentage(40);
                        document.add(lineSeparator);
                        addNewItem(document, hotelPurchase.getDescription(), Element.ALIGN_UNDEFINED, new Font(fontName, 12.0f, Font.NORMAL, BaseColor.BLACK));

                        qrCodeText = "This is an official receipt number: " + hotelPurchase.getKey() + " for room rentals for hotel: " + hotelPurchase.getHotelName() + ", TourExpert is associated and recognized by" +
                                " The International Hotel & Restaurant Association (IHRA)";
                    }

                    // create a qr code with the appropriate content:
                    BarcodeQRCode barcodeQRCode = new BarcodeQRCode(qrCodeText, 1000, 1000, null);
                    Image codeQrImage = barcodeQRCode.getImage();
                    codeQrImage.setAlignment(Element.ALIGN_CENTER);
                    codeQrImage.scaleAbsolute(100, 100);
                    codeQrImage.setSpacingBefore(-40);

                    // append the qr code to the end of the file:
                    document.add(codeQrImage);

                    // close the PDF file (which will also save the written content):
                    document.close();

                    // receipt was made successfully, return true:
                    return true;
                } else {
                    // receipt copy already exists:
                    appGeneralActivities.displayAlertDialog(source, "receipt copy was found", "copy of the requested receipt was found, please check your:\n" +
                            "Internal storage/Documents directory to watch it", R.drawable.warning);
                    return false;
                }

            } catch (FileNotFoundException | DocumentException e) {
                e.printStackTrace();
                // exception was thrown, return false;
                return false;
            }
        } else {
            // explain to the user that he should grant storage permissions to
            // create a copy of a receipt:
            appGeneralActivities.displayAlertDialog(source, "permission denied",
                    "please grant \"storage\" permission to the Application to create a copy of this receipt", R.drawable.error);
            // no storage access was granted, return false:
            return false;
        }
    }

    private void addNewItem(Document document, String text, int align, Font font) throws DocumentException {
        Chunk chunk = new Chunk(text, font);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setSpacingAfter(20);
        paragraph.setAlignment(align);
        document.add(paragraph);
    }

}
