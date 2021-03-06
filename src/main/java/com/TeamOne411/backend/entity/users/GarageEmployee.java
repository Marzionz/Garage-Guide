package com.TeamOne411.backend.entity.users;

import com.TeamOne411.backend.entity.Garage;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class GarageEmployee extends User {
    @NotNull
    private boolean isAdmin = false;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Garage getGarage() {
        return garage;
    }

    public void setGarage(Garage garage) {
        this.garage = garage;
    }
}
