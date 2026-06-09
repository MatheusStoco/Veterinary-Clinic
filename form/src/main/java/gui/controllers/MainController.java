package gui.controllers;

import database.exception.BusinessException;
import database.exception.ClinicException;
import database.model.AnimalEntity;
import database.model.MedicEntity;
import database.model.SurgeryEntity;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import service.ClinicService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * RF-01: MainController tem uma única responsabilidade —
 * receber eventos da UI e delegar à ClinicService.
 */
public class MainController implements Initializable {

    /* ── Campos do formulário ── */
    @FXML private ComboBox<String>        speciesList;
    @FXML private ComboBox<AnimalEntity>  breedList;
    @FXML private ComboBox<MedicEntity>   medicList;
    @FXML private ComboBox<SurgeryEntity> surgeryList;
    @FXML private ComboBox<LocalTime>     hourList;
    @FXML private DatePicker              datePicker;
    @FXML private Text                    surgeryTime;
    @FXML private TextField               firstName;
    @FXML private TextField               lastName;
    @FXML private TextField               tin;
    @FXML private TextField               phoneNumber;
    @FXML private TextField               email;
    @FXML private TextArea                notes;

    /* ── Avisos de validação ── */
    @FXML private Text firstNameWarning;
    @FXML private Text lastNameWarning;
    @FXML private Text tinWarning;
    @FXML private Text phoneNumberWarning;
    @FXML private Text emailWarning;

    /* ── RF-01: único ponto de acesso à camada de negócio ── */
    private final ClinicService clinicService = new ClinicService();

    private final List<AnimalEntity> allAnimals = new java.util.ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<String> especies = clinicService.buscarEspecies();
        allAnimals.addAll(clinicService.buscarAnimais());

        speciesList.getItems().addAll(especies);
        breedList.getItems().addAll(allAnimals);
        medicList.getItems().addAll(clinicService.buscarMedicos());
        surgeryList.getItems().addAll(clinicService.buscarProcedimentos());

        datePicker.setDayCellFactory(buildDayCellFactory());
    }

    @FXML
    public void onMedicSelected(ActionEvent event) {
        MedicEntity medico     = medicList.getSelectionModel().getSelectedItem();
        SurgeryEntity proc     = surgeryList.getSelectionModel().getSelectedItem();
        if (medico != null && proc != null) {
            clinicService.configurarAgenda(medico, proc);
        }
        clearAppointmentDate();
    }

    @FXML
    public void onSelectSurgery(ActionEvent event) {
        SurgeryEntity proc = surgeryList.getSelectionModel().getSelectedItem();
        if (proc == null) return;

        surgeryTime.setText("Tempo estimado: " + proc.getTime().getHour() + " hora(s)");

        MedicEntity medico = medicList.getSelectionModel().getSelectedItem();
        if (medico != null) {
            clinicService.configurarAgenda(medico, proc);
        }
        clearAppointmentDate();
    }

    @FXML
    public void onDateSelect(ActionEvent event) {
        if (datePicker.getValue() == null) return;

        hourList.getItems().clear();
        hourList.getItems().addAll(clinicService.buscarHorariosLivres(datePicker.getValue()));
    }

    @FXML
    public void onSelectSpecies(ActionEvent event) {
        Platform.runLater(() -> {
            if (speciesList.getValue() == null) return;

            List<AnimalEntity> filtrados = allAnimals.stream()
                    .filter(a -> a.getSpecies().equals(speciesList.getValue()))
                    .collect(Collectors.toList());

            breedList.getItems().setAll(filtrados);
        });
    }

    @FXML
    public void onSelectBreed(ActionEvent event) {
        if (breedList.getValue() == null) return;
        AnimalEntity selecionado = breedList.getValue();
        speciesList.setValue(selecionado.getSpecies());
        Platform.runLater(() -> breedList.setValue(selecionado));
    }

    @FXML
    public void onSubmitClicked(MouseEvent event) {
        markInvalidFields();
        if (!areAllFieldsValid()) return;

        try {
            clinicService.agendarConsulta(
                    firstName.getText(), lastName.getText(),
                    tin.getText(), phoneNumber.getText(), email.getText(),
                    breedList.getValue(), medicList.getValue(),
                    surgeryList.getValue(), datePicker.getValue(),
                    hourList.getValue(), notes.getText()
            );

            Window window = ((Node) event.getTarget()).getScene().getWindow();
            loadSubmittedPage(window);

        } catch (BusinessException e) {
            showAlert(Alert.AlertType.WARNING, "Dados inválidos", e.getMessage());
        } catch (ClinicException e) {
            showAlert(Alert.AlertType.ERROR, "Erro ao salvar", "Não foi possível realizar o agendamento.\n" + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de navegação", "Não foi possível carregar a tela de confirmação.");
        }
    }

    // ── Helpers de UI ──────────────────────────────────────────

    private void clearAppointmentDate() {
        datePicker.setValue(null);
        hourList.setValue(null);
        hourList.getItems().clear();
    }

    private void loadSubmittedPage(Window window) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/gui/submitted.fxml"));
        Parent submitted  = loader.load();

        double height      = window.getHeight();
        double width       = window.getWidth();
        double x           = window.getX();
        double y           = window.getY();
        boolean fullscreen = ((Stage) window).isFullScreen();

        Stage stage = (Stage) window;
        stage.setScene(new Scene(submitted));
        stage.setHeight(height);
        stage.setWidth(width);
        stage.setX(x);
        stage.setY(y);
        stage.setFullScreen(fullscreen);
        stage.show();
    }

    private Callback<DatePicker, DateCell> buildDayCellFactory() {
        return dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                boolean foraPeriodo = item.isBefore(LocalDate.now())
                        || item.isAfter(LocalDate.now().plusMonths(3));
                boolean semDisponibilidade = !clinicService.verificarDisponibilidade(item);

                if (foraPeriodo || semDisponibilidade) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        };
    }

    private boolean areAllFieldsValid() {
        return !firstName.getText().isEmpty()
                && !lastName.getText().isEmpty()
                && !tin.getText().isEmpty()
                && !phoneNumber.getText().isEmpty()
                && !email.getText().isEmpty()
                && speciesList.getValue() != null
                && breedList.getValue() != null
                && medicList.getValue() != null
                && surgeryList.getValue() != null
                && datePicker.getValue() != null
                && hourList.getValue() != null;
    }

    private void markInvalidFields() {
        firstNameWarning.setVisible(firstName.getText().isEmpty());
        lastNameWarning.setVisible(lastName.getText().isEmpty());
        tinWarning.setVisible(tin.getText().isEmpty());
        phoneNumberWarning.setVisible(phoneNumber.getText().isEmpty());
        emailWarning.setVisible(email.getText().isEmpty());

        if (speciesList.getValue() == null)  speciesList.setPromptText("* Obrigatório");
        if (breedList.getValue() == null)    breedList.setPromptText("* Obrigatório");
        if (medicList.getValue() == null)    medicList.setPromptText("* Obrigatório");
        if (surgeryList.getValue() == null)  surgeryList.setPromptText("* Obrigatório");
        if (datePicker.getValue() == null)   datePicker.setPromptText("* Obrigatório");
        if (hourList.getValue() == null)     hourList.setPromptText("* Obrigatório");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /* ── Limpa avisos ao digitar ── */
    @FXML public void onFirstNameKeyPressed(KeyEvent e)   { firstNameWarning.setVisible(false); }
    @FXML public void onLastNameKeyPressed(KeyEvent e)    { lastNameWarning.setVisible(false); }
    @FXML public void onTinKeyPressed(KeyEvent e)         { tinWarning.setVisible(false); }
    @FXML public void onPhoneNumberKeyPressed(KeyEvent e) { phoneNumberWarning.setVisible(false); }
    @FXML public void onEmailKeyPressed(KeyEvent e)       { emailWarning.setVisible(false); }
}
