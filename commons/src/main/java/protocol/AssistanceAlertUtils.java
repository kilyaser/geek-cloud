package protocol;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AssistanceAlertUtils extends Alert {


    public AssistanceAlertUtils(AlertType alertType) {
        super(alertType);
    }

    public AssistanceAlertUtils(AlertType alertType, String s, ButtonType... buttonTypes) {
        super(alertType, s, buttonTypes);
    }

    public static Alert getWarningConfirm(String fileName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting file");
        alert.setContentText("Are you sure want to delete the file " + fileName + " ?");
        return alert;
    }
    public static Alert getInformationAlert(String fileName, boolean confirm) {
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        if (confirm) {
            infoAlert.setTitle("Deleting file");
            infoAlert.setContentText("file " + fileName + " was deleted");
        } else {
            infoAlert.setTitle("Deletion cancelled");
            infoAlert.setContentText("Deletion process cancelled");
        }
        return infoAlert;
    }
}
