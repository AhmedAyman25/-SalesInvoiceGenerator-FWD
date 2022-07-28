/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.udacity.controller;

import com.udacity.model.InvoiceHeader;
import com.udacity.model.InvoiceHeaderTableModel;
import com.udacity.model.InvoiceLine;
import com.udacity.model.InvoiceLinesTableModel;
import com.udacity.view.InvoiceFrame;
import com.udacity.view.InvoiceHeaderDialog;
import com.udacity.view.InvoiceLineDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Ahmed
 */
public class SalesInvoiceListener implements ActionListener, ListSelectionListener  {
    private InvoiceFrame frame;
    private DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    
    public SalesInvoiceListener(InvoiceFrame frame) {
        this.frame = frame;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {
            case "CreateNewInvoice":
                displayNewInvoiceDialog();
                break;
            case "DeleteInvoice":
                deleteInvoice();
                break;
            case "CreateNewLine":
                displayNewLineDialog();
                break;
            case "DeleteLine":
                deleteLine();
                break;
            case "LoadFile":
                loadFile();
                break;
            case "SaveFile":
                saveData();
                break;
            case "createInvCancel":
                createInvCancel();
                break;
            case "createInvOK":
                createInvOK();
                break;
            case "createLineCancel":
                createLineCancel();
                break;
            case "createLineOK":
                createLineOK();
                break;

        }
    }

    private void loadFile() {
        JOptionPane.showMessageDialog(frame, "Please, select header file!", "Attention", JOptionPane.WARNING_MESSAGE);
        JFileChooser openFile = new JFileChooser();
        int result = openFile.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File headerFile = openFile.getSelectedFile();
            try {
                FileReader headerFr = new FileReader(headerFile);
                BufferedReader headerBr = new BufferedReader(headerFr);
                String headerLine = null;

                while ((headerLine = headerBr.readLine()) != null) {
                    String[] headerParts = headerLine.split(",");
                    String invNumStr = headerParts[0];
                    String invDateStr = headerParts[1];
                    String custName = headerParts[2];

                    int invNum = Integer.parseInt(invNumStr);
                    Date invDate = df.parse(invDateStr);

                    InvoiceHeader inv = new InvoiceHeader(invNum, custName, invDate);
                    frame.getInvoicesList().add(inv);
                }

                JOptionPane.showMessageDialog(frame, "Please, select lines file!", "Attension", JOptionPane.WARNING_MESSAGE);
                result = openFile.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File linesFile = openFile.getSelectedFile();
                    BufferedReader linesBr = new BufferedReader(new FileReader(linesFile));
                    String linesLine = null;
                    while ((linesLine = linesBr.readLine()) != null) {
                        String[] lineParts = linesLine.split(",");
                        String invNumStr = lineParts[0];
                        String itemName = lineParts[1];
                        String itemPriceStr = lineParts[2];
                        String itemCountStr = lineParts[3];

                        int invNum = Integer.parseInt(invNumStr);
                        double itemPrice = Double.parseDouble(itemPriceStr);
                        int itemCount = Integer.parseInt(itemCountStr);
                        InvoiceHeader header = findInvoiceByNum(invNum);
                        InvoiceLine invLine = new InvoiceLine(itemName, itemPrice, itemCount, header);
                        header.getLines().add(invLine);
                    }
                    frame.setInvoiceHeaderTableModel(new InvoiceHeaderTableModel(frame.getInvoicesList()));
                    frame.getInvoicesTable().setModel(frame.getInvoiceHeaderTableModel());
                    frame.getInvoicesTable().validate();
                }
                System.out.println("Check");
            } catch (ParseException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Date Format Error\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Number Format Error\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "File Error\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Read Error\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        displayInvoices();
    }

    private void saveData() {
        String headers = "";
        String lines = "";
        for (InvoiceHeader header : frame.getInvoicesList()) {
            headers += header.getDataAsCSV();
            headers += "\n";
            for (InvoiceLine line : header.getLines()) {
                lines += line.getDataAsCSV();
                lines += "\n";
            }
        }
        JOptionPane.showMessageDialog(frame, "Please, select file to save header data!", "Attension", JOptionPane.WARNING_MESSAGE);
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File headerFile = fileChooser.getSelectedFile();
            try {
                FileWriter hFW = new FileWriter(headerFile);
                hFW.write(headers);
                hFW.flush();
                hFW.close();

                JOptionPane.showMessageDialog(frame, "Please, select file to save lines data!", "Attension", JOptionPane.WARNING_MESSAGE);
                result = fileChooser.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File linesFile = fileChooser.getSelectedFile();
                    FileWriter lFW = new FileWriter(linesFile);
                    lFW.write(lines);
                    lFW.flush();
                    lFW.close();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(frame, "Data saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

    }

    private InvoiceHeader findInvoiceByNum(int invNum) {
        InvoiceHeader header = null;
        for (InvoiceHeader inv : frame.getInvoicesList()) {
            if (invNum == inv.getInvNum()) {
                header = inv;
                break;
            }
        }
        return header;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        System.out.println("Invoice Selected!");
        invoicesTableRowSelected();
    }

    private void invoicesTableRowSelected() {
        int selectedRowIndex = frame.getInvoicesTable().getSelectedRow();
        if (selectedRowIndex >= 0) {
            InvoiceHeader row = frame.getInvoiceHeaderTableModel().getInvoicesList().get(selectedRowIndex);
            frame.getCstNameTF().setText(row.getCustomerName());
            frame.getInvDateTF().setText(df.format(row.getInvDate()));
            frame.getInvNumLbl().setText("" + row.getInvNum());
            frame.getInvTotalLbl().setText("" + row.getInvTotal());
            ArrayList<InvoiceLine> lines = row.getLines();
            frame.setInvoiceLinesTableModel(new InvoiceLinesTableModel(lines));
            frame.getInvLinesTable().setModel(frame.getInvoiceLinesTableModel());
            frame.getInvoiceLinesTableModel().fireTableDataChanged();
        }
    }

    private void displayNewInvoiceDialog() {
        frame.setHeaderDialog(new InvoiceHeaderDialog(frame));
        frame.getHeaderDialog().setVisible(true);
    }

    private void displayNewLineDialog() {
        frame.setLineDialog(new InvoiceLineDialog(frame));
        frame.getLineDialog().setVisible(true);
    }

    private void createInvCancel() {
        frame.getHeaderDialog().setVisible(false);
        frame.getHeaderDialog().dispose();
        frame.setHeaderDialog(null);
    }

    private void createInvOK() {
        String cstName = frame.getHeaderDialog().getCustNameField().getText();
        String invDateStr = frame.getHeaderDialog().getInvDateField().getText();
        frame.getHeaderDialog().setVisible(false);
        frame.getHeaderDialog().dispose();
        frame.setHeaderDialog(null);
        try {
            Date invDate = df.parse(invDateStr);
            int invNum = getNextInvoiceNum();
            InvoiceHeader invoiceHeader = new InvoiceHeader(invNum, cstName, invDate);
            frame.getInvoicesList().add(invoiceHeader);
            frame.getInvoiceHeaderTableModel().fireTableDataChanged();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        displayInvoices();
    }

    private int getNextInvoiceNum() {
        int max = 0;
        for (InvoiceHeader header : frame.getInvoicesList()) {
            if (header.getInvNum() > max) {
                max = header.getInvNum();
            }
        }
        return (max + 1);
    }

    private void createLineCancel() {
        frame.getLineDialog().setVisible(false);
        frame.getLineDialog().dispose();
        frame.setLineDialog(null);
    }

    private void createLineOK() {
        String itemName = frame.getLineDialog().getItemNameField().getText();
        String itemCountStr = frame.getLineDialog().getItemCountField().getText();
        String itemPriceStr = frame.getLineDialog().getItemPriceField().getText();
        frame.getLineDialog().setVisible(false);
        frame.getLineDialog().dispose();
        frame.setLineDialog(null);
        int itemCount = Integer.parseInt(itemCountStr);
        double itemPrice = Double.parseDouble(itemPriceStr);
        int headerIndex = frame.getInvoicesTable().getSelectedRow();
        InvoiceHeader invoice = frame.getInvoiceHeaderTableModel().getInvoicesList().get(headerIndex);

        InvoiceLine invoiceLine = new InvoiceLine(itemName, itemPrice, itemCount, invoice);
        invoice.addInvLine(invoiceLine);
        frame.getInvoiceLinesTableModel().fireTableDataChanged();
        frame.getInvoiceHeaderTableModel().fireTableDataChanged();
        frame.getInvTotalLbl().setText("" + invoice.getInvTotal());
        displayInvoices();
    }

    private void deleteInvoice() {
        int invIndex = frame.getInvoicesTable().getSelectedRow();
        InvoiceHeader header = frame.getInvoiceHeaderTableModel().getInvoicesList().get(invIndex);
        frame.getInvoiceHeaderTableModel().getInvoicesList().remove(invIndex);
        frame.getInvoiceHeaderTableModel().fireTableDataChanged();
        frame.setInvoiceLinesTableModel(new InvoiceLinesTableModel(new ArrayList<InvoiceLine>()));
        frame.getInvLinesTable().setModel(frame.getInvoiceLinesTableModel());
        frame.getInvoiceLinesTableModel().fireTableDataChanged();
        frame.getCstNameTF().setText("");
        frame.getInvDateTF().setText("");
        frame.getInvNumLbl().setText("");
        frame.getInvTotalLbl().setText("");
        displayInvoices();
    }

    private void deleteLine() {
        int lineIndex = frame.getInvLinesTable().getSelectedRow();
        InvoiceLine line = frame.getInvoiceLinesTableModel().getInvoiceLines().get(lineIndex);
        frame.getInvoiceLinesTableModel().getInvoiceLines().remove(lineIndex);
        frame.getInvoiceLinesTableModel().fireTableDataChanged();
        frame.getInvoiceHeaderTableModel().fireTableDataChanged();
        frame.getInvTotalLbl().setText("" + line.getHeader().getInvTotal());
        displayInvoices();
    }

    private void displayInvoices() {
        System.out.println("***************************");
        for (InvoiceHeader header : frame.getInvoicesList()) {
            System.out.println(header);
        }
        System.out.println("***************************");
    }
    
}
