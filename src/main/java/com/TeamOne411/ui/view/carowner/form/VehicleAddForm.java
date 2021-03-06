package com.TeamOne411.ui.view.carowner.form;

import com.TeamOne411.backend.entity.Vehicle;
import com.TeamOne411.backend.service.api.car.ApiVehicleService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehicleAddForm extends VerticalLayout {

    //these need to be lists but vehicle API needs work
    private ComboBox<String> make = new ComboBox<String>("Make");
    private ComboBox<String> model = new ComboBox<String>("Model");
    private ComboBox<String> year = new ComboBox<String>("Year");
    private TextField vin = new TextField("VIN");
    private Button backButton = new Button("Back To My Information", new Icon(VaadinIcon.ARROW_LEFT));
    private Button nextButton = new Button("Confirm Details", new Icon(VaadinIcon.ARROW_RIGHT));
    private ShortcutRegistration enterKeyRegistration;

    Binder<Vehicle> binder = new BeanValidationBinder<>(Vehicle.class);
    private Vehicle vehicle = new Vehicle();
    private ApiVehicleService apiVehicleService;
    private boolean isEditMode = false;

    public VehicleAddForm(ApiVehicleService apiVehicleService) {
        this.apiVehicleService = apiVehicleService;
        // initial view setup
        addClassName("car-add-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        nextButton.setIconAfterText(true);
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        binder.bindInstanceFields(this);

        make.setEnabled(false);
        model.setEnabled(false);

        fillYearComboBox();


        //when year is chosen, fill make
        year.addValueChangeListener(e -> {
            if (year.getValue() != null) {
                make.setEnabled(true);
                fillMakeComboBox();
            }
        });

        //when make is chosen, fill model
        make.addValueChangeListener(e -> {
            if (make.getValue() != null) {
                model.setEnabled(true);
                fillModelComboBox(make.getValue());
            }
        });



        // set button click listeners
        backButton.addClickListener(e -> fireEvent(new BackEvent(this)));
        nextButton.addClickListener(e -> validateAndFireNext());

        vin.setValueChangeMode(ValueChangeMode.LAZY);
        vin.setPlaceholder("1ABCD12ABCD123456");

        add(
                new H3("Tell us about your car"),
                year,
                make,
                model,
                vin,
                new HorizontalLayout(backButton, nextButton)
        );
    }

    private void validateAndFireNext() {
        binder.validate();
        if (!binder.isValid()) return;
        fireEvent(new VehicleAddForm.NextEvent(this));
    }

    public void fillMakeComboBox(){
        try {
            this.make.setItems(apiVehicleService.getAllMakes());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void fillModelComboBox(String make){
        try {
            this.model.setItems(apiVehicleService.getModelsForMake(make));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void fillYearComboBox(){
        int year = LocalDate.now().getYear();
        List<String> years = new ArrayList<>();
        for (int i = 1980; i <= year; i++){
            years.add(Integer.toString(i));
        }

        this.year.setItems(years);
    }

    public Vehicle getValidCar() {
        try {
            binder.writeBean(vehicle);
            return vehicle;
        } catch (ValidationException e) {
            e.printStackTrace();
        }

        // return null if try block fails for any reason
        return null;
    }

    public void setEnterShortcutRegistration(boolean addRegistration) {
        if (addRegistration) enterKeyRegistration = nextButton.addClickShortcut(Key.ENTER);
        else if (enterKeyRegistration != null) enterKeyRegistration.remove();
    }

    /**
     * Fills all form controls with known details of an existing vehicle.
     * @param vehicle the vehicle to fill details for
     */
    public void prefillForm(Vehicle vehicle) {
        this.vehicle = vehicle;
        //binder.readBean(this.vehicle);

        fillYearComboBox();
        year.setValue(vehicle.getYear());

        fillMakeComboBox();
        make.setValue(vehicle.getMake());

        fillModelComboBox(vehicle.getMake());
        model.setValue(vehicle.getModel());

        vin.setValue(vehicle.getVin());
    }

    /**
     * Setter for the isEditMode field.
     * @param isEditMode
     */
    public void setIsEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    /**
     * Setter for the text of the back button
     * @param text text to set for the button
     */
    public void setBackButtonText(String text) {
        backButton.setText(text);
    }

    /**
     * Setter for the text of the next button.
     * @param text text to set for the button
     */
    public void setNextButtonText(String text) {
        nextButton.setText(text);
    }



    // Button event definitions begin
    public static class BackEvent extends ComponentEvent<VehicleAddForm> {
        BackEvent(VehicleAddForm source) {
            super(source, false);
        }
    }

    public static class NextEvent extends ComponentEvent<VehicleAddForm> {
        NextEvent(VehicleAddForm source) {
            super(source, false);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
    // Button event definitions end
}
