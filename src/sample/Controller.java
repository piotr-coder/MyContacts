package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import sample.datamodel.Contact;
import sample.datamodel.ContactData;
import java.io.IOException;
import java.util.Optional;

public class Controller {
    @FXML
    private BorderPane mainPanel;
    @FXML
    private TableView<Contact> contactsTable;
    @FXML
    private TableColumn<Contact, String> colLastName;
    @FXML
    private TableColumn<Contact, String> colFirstName;
    @FXML
    private TableColumn<Contact, String> colPhone;
    @FXML
    private TableColumn<Contact, String> colNotes;

    private ContactData data;

    public void initialize() {
        initializeTableView();
        initializeContextMenu();
        setTableViewEditable();
    }

    private void initializeTableView(){
        data = new ContactData();
        data.loadContacts();
        contactsTable.setItems(data.getContacts());
        colLastName.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }
    private void initializeContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("delete");
        deleteMenuItem.setOnAction(actionEvent -> deleteContact());
        contextMenu.getItems().addAll(deleteMenuItem);
        contactsTable.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(contactsTable, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });
    }
    private void setTableViewEditable(){
        contactsTable.setEditable(true);
        colLastName.setCellFactory(TextFieldTableCell.forTableColumn());
        colFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        colNotes.setCellFactory(TextFieldTableCell.forTableColumn());
        colPhone.setCellFactory(TextFieldTableCell.forTableColumn());
    }
    @FXML
    public void onEditChanged(TableColumn.CellEditEvent<Contact, String> changed){
        Contact contact = contactsTable.getSelectionModel().getSelectedItem();
        if(changed.getSource().equals(colFirstName)){
            contact.setFirstName(changed.getNewValue());
        }else if (changed.getSource().equals(colLastName)) {
            contact.setLastName(changed.getNewValue());
        }else if(changed.getSource().equals(colPhone)){
            contact.setPhoneNumber(changed.getNewValue());
        }else if(changed.getSource().equals(colNotes)){
            contact.setNotes(changed.getNewValue());
        }
        data.saveContacts();
    }
    @FXML
    public void showAddContactDialog() {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Add New Contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactdialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch(IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.setHeaderText("Fill in the information for the new Contact");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            ContactController contactController = fxmlLoader.getController();
            data.addContact(contactController.getNewContact());
            data.saveContacts();
        }
    }
    @FXML
    public void showEditContactDialog() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if(selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Edit Contact");
        dialog.setHeaderText("Fill in the information you want to edit");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactdialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        ContactController contactController = fxmlLoader.getController();
        contactController.editContact(selectedContact);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            contactController.updateContact(selectedContact);
            data.saveContacts();
        }
    }

    public void deleteContact() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if(selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to delete.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Contact");
        alert.setHeaderText("Are you sure you want to delete the selected contact:\n");
        alert.setContentText(selectedContact.getFirstName() + " " + selectedContact.getLastName());

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            data.deleteContact(selectedContact);
            data.saveContacts();
        }
    }
    public void handleExit(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            data.saveContacts();
            Platform.exit();
        }
    }
}