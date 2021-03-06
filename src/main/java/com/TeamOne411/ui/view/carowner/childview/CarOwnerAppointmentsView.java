package com.TeamOne411.ui.view.carowner.childview;

import com.TeamOne411.backend.entity.Garage;
import com.TeamOne411.backend.entity.Vehicle;
import com.TeamOne411.backend.entity.schedule.Appointment;
import com.TeamOne411.backend.entity.users.CarOwner;
import com.TeamOne411.backend.service.*;
import com.TeamOne411.ui.utils.FormattingUtils;
import com.TeamOne411.ui.view.carowner.form.AppointmentDialog;
import com.TeamOne411.ui.view.carowner.form.VehicleHistoryDialog;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;


/**
 * This class displays the booked appointments for the logged in car owner.
 */
public class CarOwnerAppointmentsView extends VerticalLayout {
    private final AppointmentService appointmentService;
    private final Grid<Appointment> appointmentsToday = new Grid<>(Appointment.class);
    private final Grid<Appointment> upcomingAppointments = new Grid<>(Appointment.class);
    private final H5 noAppointmentsToday = new H5("You do not have any appointments scheduled for today");
    private final H5 noUpcomingAppointments = new H5("You do not have any upcoming appointments scheduled");
    private final CarOwner carOwner;
    private AppointmentDialog appointmentDialog;

    public CarOwnerAppointmentsView(AppointmentService appointmentService,
                                    ServiceCatalogService serviceCatalogService,
                                    GarageCalendarService garageCalendarService,
                                    VehicleService vehicleService,
                                    CarOwner carOwner) {
        this.appointmentService = appointmentService;
        this.carOwner = carOwner;

        // new appointment button setup and click listener
        Button newAppointment = new Button("Schedule New Appointment");
        newAppointment.addClickListener(e -> showAppointmentDialog(serviceCatalogService, garageCalendarService,
                vehicleService, carOwner));

        // H5 message setup
        noAppointmentsToday.setVisible(false);
        noUpcomingAppointments.setVisible(false);

        // GRID SETUP
        // setup the appointments for today grid
        setGridAttributes(appointmentsToday, "today-grid");
        appointmentsToday.addColumn(appointment -> getVehicleInfo(appointment.getVehicle())).setSortable(false)
                .setHeader("Vehicle").setKey("vehicle");
        appointmentsToday.addColumn(appointment -> {
            Garage garage = appointment.getGarage();
            return garage.getCompanyName();
        }).setSortable(false).setHeader("Garage").setKey("garage");
        appointmentsToday.addColumn(appointment -> FormattingUtils.HOUR_FORMATTER
                .format(appointment.getAppointmentTime())).setHeader("Time").setKey("appointmentTime").setSortable(false);
        appointmentsToday.addColumn(Appointment::getStatus).setSortable(false).setHeader("Status").setKey("status");
        appointmentsToday.addColumn(appointment ->
                FormattingUtils.convertTime(appointment.getEstimatedCompletionTime())).setHeader("Estimated Completion")
                .setKey("estimatedCompletionTime").setSortable(false);
        appointmentsToday.addColumn(Appointment::getStatusComments).setHeader("Garage Comments")
                .setKey("statusComments").setSortable(false);
        appointmentsToday.addComponentColumn(this::viewHistory).setHeader("Services Completed")
                .setTextAlign(ColumnTextAlign.CENTER).setFlexGrow(0);
        appointmentsToday.getColumns().forEach(col -> col.setAutoWidth(true));


        // setup the upcoming appointments grid
        setGridAttributes(upcomingAppointments, "upcoming-grid");
        upcomingAppointments.addColumn(appointment -> getVehicleInfo(appointment.getVehicle())).setSortable(false)
                .setHeader("Vehicle").setKey("vehicle");
        upcomingAppointments.addColumn(appointment -> {
            Garage garage = appointment.getGarage();
            return garage.getCompanyName();
        }).setSortable(false).setHeader("Garage").setKey("garage");
        upcomingAppointments.addColumn(appointment -> FormattingUtils.SHORT_DATE_FORMATTER
                .format(appointment.getAppointmentDate())).setHeader("Date").setKey("appointmentDate").setSortable(false);
        upcomingAppointments.addColumn(appointment -> FormattingUtils.HOUR_FORMATTER
                .format(appointment.getAppointmentTime())).setHeader("Time").setKey("appointmentTime").setSortable(false);
        upcomingAppointments.addComponentColumn(this::cancelButton).setHeader("Cancel")
                .setTextAlign(ColumnTextAlign.CENTER);
        upcomingAppointments.getColumns().forEach(col -> col.setAutoWidth(true));

        // LAYOUTS
        // create the layout for appointments Today
        VerticalLayout todayLayout = new VerticalLayout(
                new H4("Today"),
                noAppointmentsToday,
                appointmentsToday
        );
        setLayoutAttributes(todayLayout, "appointments-today", "60%");

        // create the layout for the upcoming appointments
        VerticalLayout upcomingLayout = new VerticalLayout(
                new H4("Upcoming Appointments"),
                noUpcomingAppointments,
                upcomingAppointments
        );
        setLayoutAttributes(upcomingLayout, "upcoming-appointments", "40%");

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setWidthFull();
        splitLayout.addToPrimary(todayLayout);
        splitLayout.addToSecondary(upcomingLayout);
        splitLayout.setSplitterPosition(60);

        // add new appointment button and the combined layout to the class layout
        add(newAppointment, splitLayout);

        // populate the grids
        updateTodayGrid();
        updateUpcomingGrid();
    }

    /**
     * Concatenates the vehicle year, make and model for display in the grid
     *
     * @param vehicle the vehicle to get info on
     * @return the concatenated string
     */
    private String getVehicleInfo(Vehicle vehicle) {
        return vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
    }

    /**
     * refreshes the appointments today grid
     */
    private void updateTodayGrid() {
        if (appointmentService.findAllAppointmentsForToday(carOwner).isEmpty()) {
            appointmentsToday.setVisible(false);
            noAppointmentsToday.setVisible(true);
        } else {
            appointmentsToday.setItems(appointmentService.findAllAppointmentsForToday(carOwner));
            appointmentsToday.setVisible(true);
            noAppointmentsToday.setVisible(false);
        }
    }

    /**
     * refreshes the upcoming appointments grid
     */
    private void updateUpcomingGrid() {
        if (appointmentService.findAllUpcomingAppointments(carOwner).isEmpty()) {
            upcomingAppointments.setVisible(false);
            noUpcomingAppointments.setVisible(true);
        } else {
            upcomingAppointments.setItems(appointmentService.findAllUpcomingAppointments(carOwner));
            upcomingAppointments.setVisible(true);
            noUpcomingAppointments.setVisible(false);
        }
    }

    /**
     * sets common grid attributes
     */
    private void setGridAttributes(Grid<Appointment> grid, String className) {
        grid.setClassName(className);
        grid.removeAllColumns();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightByRows(true);
    }

    /**
     * Sets common attributes for the layouts
     */
    private void setLayoutAttributes(VerticalLayout verticalLayout, String className, String width) {
        verticalLayout.setClassName(className);
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.getStyle().set("border", "1px solid #9E9E9E");
        verticalLayout.setPadding(false);
        verticalLayout.setSpacing(false);
        verticalLayout.setWidth(width);
    }

    /**
     * Creates the cancel icon button for each row in the grid
     *
     * @param appointment the Appointment instance the icon button is associated with
     * @return the icon button to be returned
     */
    private Button cancelButton(Appointment appointment) {
        Button deleteButton = new Button(VaadinIcon.MINUS_CIRCLE_O.create(), buttonClickEvent ->
                cancelClick(appointment));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return deleteButton;
    }

    /**
     * Fired on cancelClick(). Shows a confirm dialog and then cancels the selected appointment & removes from the grid
     */
    private void cancelClick(Appointment appointment) {
        String message = "Are you sure you want to cancel this appointment?";
        ConfirmDialog confirmDeleteDialog = new ConfirmDialog(
                "Cancel Appointment", message,
                "Proceed",
                e -> onCancelConfirm(appointment),
                "Exit",
                e -> e.getSource().close());

        confirmDeleteDialog.setConfirmButtonTheme("error primary");
        confirmDeleteDialog.open();
    }

    /**
     * Fired when cancel confirm dialog is confirmed by user. Cancels the appointment and clears the time slots
     */
    private void onCancelConfirm(Appointment appointment) {
        if (appointment != null) {
            appointmentService.deleteAppointmentTasks(appointment);
            appointmentService.deleteAppointment(appointment);
            String successMessage = "Your appointment has been cancelled.";
            Notification notification = new Notification(
                    successMessage,
                    4000,
                    Notification.Position.TOP_END
            );
            notification.open();
        }
        updateUpcomingGrid();
    }

    /**
     * Opens the appointment dialog
     */
    private void showAppointmentDialog(ServiceCatalogService serviceCatalogService,
                                       GarageCalendarService garageCalendarService,
                                       VehicleService vehicleService,
                                       CarOwner carOwner) {
        appointmentDialog = new AppointmentDialog(appointmentService, serviceCatalogService,
                garageCalendarService, vehicleService, carOwner);
        appointmentDialog.setWidth("50%");
        appointmentDialog.setHeightFull();
        appointmentDialog.addListener(AppointmentDialog.SaveSuccessEvent.class,
                this::onSave);
        appointmentDialog.open();
    }

    /**
     * Fired when the AppointmentForm has been exited.
     *
     * @param event the event that fired this method
     */
    private void onSave(ComponentEvent<AppointmentDialog> event) {
        appointmentDialog.close();
        updateUpcomingGrid();
        updateTodayGrid();

        String successMessage = "Your appointment has been booked. Thank you for using Garage Guide.";
        Notification notification = new Notification(
                successMessage,
                4000,
                Notification.Position.MIDDLE
        );
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }

    /**
     * Creates the view history icon button for each row in the grid
     *
     * @param appointment the appointment instance the icon button is associated with
     * @return the icon button to be returned
     */
    private Button viewHistory(Appointment appointment) {
        Button updateButton = new Button(VaadinIcon.CAR.create(), buttonClickEvent ->
                showVehicleHistoryDialog(appointment.getVehicle()));
        updateButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        updateButton.setEnabled(appointment.getStatus().equals("Completed"));
        return updateButton;
    }

    /**
     * Creates and opens a new VehicleHistoryDialog instance for viewing the service history for a vehicle
     */
    private void showVehicleHistoryDialog(Vehicle vehicle) {
        VehicleHistoryDialog vehicleHistoryDialog = new VehicleHistoryDialog(vehicle, appointmentService);
        vehicleHistoryDialog.setWidth("75%");
        vehicleHistoryDialog.setHeight("auto");
        vehicleHistoryDialog.open();
    }
}
