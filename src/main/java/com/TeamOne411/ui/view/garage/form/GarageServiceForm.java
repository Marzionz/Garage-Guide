package com.TeamOne411.ui.view.garage.form;

import com.TeamOne411.backend.entity.Garage;
import com.TeamOne411.backend.entity.servicecatalog.OfferedService;
import com.TeamOne411.backend.entity.servicecatalog.ServiceCategory;
import com.TeamOne411.backend.service.ServiceCatalogService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;
import com.vaadin.flow.shared.Registration;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Locale;

/**
 * This class is a VerticalLayout for adding/editing offered services for a garage
 */
public class GarageServiceForm extends VerticalLayout {

    Binder<OfferedService> binder = new BeanValidationBinder<>(OfferedService.class);
    private OfferedService offeredService = new OfferedService();

    public GarageServiceForm(ServiceCatalogService serviceCatalogService, Garage garage) {

        // initial view setup
        addClassName("garage-service-form");
        TextField serviceName = new TextField("Service name");
        serviceName.setWidth("100%");

        // centers the form contents within the window
        setAlignItems(Alignment.CENTER);

        // format Price
        TextField price = new TextField("Price");
        binder.forField(price).withConverter(new PriceConverter()).bind("price");
        binder.bindInstanceFields(this);
        price.setWidth("100%");

        // set button click listeners
        Button saveButton = new Button("Save");
        saveButton.addClickListener(e -> fireEvent(new SaveEvent(this)));
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> fireEvent(new CancelEvent(this)));

        // format Service Category
        ComboBox<ServiceCategory> serviceCategory = new ComboBox<>("Category");
        serviceCategory.setItemLabelGenerator(ServiceCategory::getCategoryName);
        serviceCategory.setItems(serviceCatalogService.findCategoriesByGarage(garage));
        serviceCategory.setWidth("100%");

        // format Duration
        ComboBox<Duration> duration = new ComboBox<>("Duration");
        duration.setItems((Duration.ofMinutes(0)), Duration.ofMinutes(30), Duration.ofMinutes(60), Duration.ofMinutes(90), Duration.ofMinutes(120),
                Duration.ofMinutes(150), Duration.ofMinutes(180));
        duration.setWidth("100%");

        // add fields to the form
        add(serviceName,
                price,
                duration,
                serviceCategory,
                new HorizontalLayout(saveButton, cancelButton));
    }

    /**
     * Fills all form controls with known details of an existing service.
     *
     * @param service the offeredService to fill details in for
     */
    public void prefillForm(OfferedService service) {
        offeredService = service;
        binder.readBean(service);
    }

    /**
     * Attempts to return an offeredService instance from the values of the form controls.
     *
     * @return an offeredService instance
     */
    public OfferedService getOfferedService() {
        try {
            binder.writeBean(offeredService);
            return offeredService;
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
     * This method formats the price as a two digit decimal
     */
    private static class PriceConverter extends StringToBigDecimalConverter {

        public PriceConverter() {
            super(BigDecimal.ZERO, "Cannot convert value to a number.");
        }

        @Override
        protected NumberFormat getFormat(Locale locale) {
            // Always display currency with two decimals
            final NumberFormat format = super.getFormat(locale);
            if (format instanceof DecimalFormat) {
                format.setMaximumFractionDigits(2);
                format.setMinimumFractionDigits(2);
            }
            return format;
        }
    }

    /**
     * Event to emit when save button is clicked
     */
    public static class SaveEvent extends ComponentEvent<GarageServiceForm> {
        SaveEvent(GarageServiceForm source) {
            super(source, false);
        }
    }

    /**
     * Event to emit when cancel button is clicked
     */
    public static class CancelEvent extends ComponentEvent<GarageServiceForm> {
        CancelEvent(GarageServiceForm source) {
            super(source, false);
        }
    }
}
