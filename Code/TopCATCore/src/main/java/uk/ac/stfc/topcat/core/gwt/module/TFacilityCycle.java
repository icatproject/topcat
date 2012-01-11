/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.core.gwt.module;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author sn65
 */
public class TFacilityCycle implements Serializable {
    String description;
    String name;
    Date   startDate;
    Date   finishDate;
    public TFacilityCycle(){
    }

    public TFacilityCycle(String description, String name, Date startDate, Date finishDate) {
        this.description = description;
        this.name = name;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
