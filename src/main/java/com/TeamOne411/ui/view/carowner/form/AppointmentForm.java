package com.TeamOne411.ui.view.carowner.form;

import com.TeamOne411.backend.entity.Garage;
import com.TeamOne411.backend.entity.schedule.Appointment;
import com.TeamOne411.backend.entity.schedule.AppointmentTask;
import com.TeamOne411.backend.entity.servicecatalog.OfferedService;
import com.TeamOne411.backend.entity.servicecatalog.ServiceCategory;
import com.TeamOne411.backend.service.AppointmentService;
import com.TeamOne411.backend.service.GarageCalendarService;
import com.TeamOne411.backend.service.GarageService;
import com.TeamOne411.backend.service.ServiceCatalogService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


public class AppointmentForm extends VerticalLayout {
    private ServiceCatalogService serviceCatalogService;
    private GarageCalendarService garageCalendarService;
    Binder<Appointment> binder = new BeanValidationBinder<>(Appointment.class);
    private Appointment appointment = new Appointment();
    private AppointmentTask appointmentTask = new AppointmentTask();
    private Duration estimatedDuration;
    private BigDecimal estimatedTotalPrice;
    private FormLayout garageForm = new FormLayout();
    private FormLayout confirmForm = new FormLayout();
    private FormLayout appointmentTimeForm = new FormLayout();
    private Accordion accordion = new Accordion();
    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");
    private final ComboBox<Garage> garage = new ComboBox<>("Select Garage");
    private final Grid<OfferedService> offeredServicesGrid = new Grid<>(OfferedService.class);
    private final DatePicker appointmentDate = new DatePicker("Desired Appointment Date");
    private final ComboBox<LocalTime> appointmentTime = new ComboBox<>("Select Available Time");
    private TextField vehicle = new TextField("Vehicle");
    private List<Set<OfferedService>> offeredServiceList = new ArrayList<java.util.Set<OfferedService>>();

    public AppointmentForm(AppointmentService appointmentService, GarageService garageService,
                           ServiceCatalogService serviceCatalogService, GarageCalendarService garageCalendarService){
        this.serviceCatalogService = serviceCatalogService;
        this.garageCalendarService = garageCalendarService;


        addClassName("appointment-form");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        binder.bindInstanceFields(this);
        accordion.setWidth("100%");
        saveButton.setEnabled(false);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // VEHICLE AND GARAGE INFO
        garageForm.add(vehicle, garage);
        vehicle.setPlaceholder("Placeholder");
        garage.setItemLabelGenerator(Garage::getCompanyName);
        garage.setItems(garageService.findAll());
        garage.setRequired(true);
        garage.setRequiredIndicatorVisible(true);
        accordion.add("Vehicle and Garage Information", garageForm);

        // OFFERED SERVICES
        offeredServicesGrid.setEnabled(false);
        offeredServicesGrid.setVisible(false);
        offeredServicesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        accordion.add("Services Required", offeredServicesGrid);

        // APPOINTMENT DATE AND TIME
        appointmentTimeForm.add(appointmentDate, appointmentTime);
        appointmentDate.setMin(LocalDate.now());
        appointmentDate.setRequired(true);
        appointmentDate.setRequiredIndicatorVisible(true);
        appointmentTime.setRequired(true);
        appointmentTime.setRequiredIndicatorVisible(true);
        appointmentTime.setEnabled(false);
        accordion.add("Appointment Date and Time", appointmentTimeForm);

        // CONFIRM DETAILS
        accordion.add("Confirmation", confirmForm);

        // CLICK LISTENERS
        garage.addValueChangeListener(e -> setOfferedServicesGrid());
        appointmentDate.addValueChangeListener(e -> setAppointmentTime());
        saveButton.addClickListener(e -> fireEvent(new AppointmentForm.SaveEvent(this)));
        cancelButton.addClickListener(e -> fireEvent(new AppointmentForm.CancelEvent(this)));

        // add fields to the form
        add(accordion, new HorizontalLayout(saveButton, cancelButton));
    }

    /**
     * Sets the offeredServicesGrid contents once a garage has been selected
     */
    private void setOfferedServicesGrid(){
        offeredServicesGrid.setItems(serviceCatalogService.findByServiceCategory_Garage(garage.getValue()));
        offeredServicesGrid.setEnabled(true);
        offeredServicesGrid.setVisible(true);
        offeredServicesGrid.removeAllColumns();
        offeredServicesGrid.addColumn(offeredService -> {
            ServiceCategory serviceCategory = offeredService.getServiceCategory();
            return serviceCategory.getCategoryName();
        }).setSortable(true).setHeader("Category").setKey("serviceCategory");
        offeredServicesGrid.addColumn(OfferedService::getServiceName).setKey("serviceName").setHeader("Service");
        final DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        offeredServicesGrid.addColumn(offeredService -> decimalFormat.format(offeredService.getPrice()))
                .setHeader("Price").setComparator(Comparator.comparing(OfferedService::getPrice))
                .setKey("price").setSortable(false);
    }

    /**
     * Sets the appointmentTime combobox once the user has selected an appointmentDate
     */
    private void setAppointmentTime(){
        // set the time slots once the user has picked a desired appointment date
        appointmentTime.setEnabled(true);
        appointmentTime.setItems(garageCalendarService
                .findStartTimeByGarageAndStartDate(garage.getValue(), appointmentDate.getValue()));
    }

    /**
     * Attempts to return an appointment instance from the values of the form controls.
     *
     * @return an appointment instance
     */
    public Appointment getAppointment() {
        try {
            binder.writeBean(appointment);
            return appointment;
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    /**
     * Event to emit when save button is clicked
     */
    public static class SaveEvent extends ComponentEvent<AppointmentForm> {
        SaveEvent(AppointmentForm source) {
            super(source, false);
        }
    }

    /**
     * Event to emit when cancel button is clicked
     */
    public static class CancelEvent extends ComponentEvent<AppointmentForm> {
        CancelEvent(AppointmentForm source) {
            super(source, false);
        }
    }
}