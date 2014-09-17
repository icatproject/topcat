package org.icatproject.topcat.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({TInvestigation.class,TInstrument.class,TDataset.class,TFacilityCycle.class})
@XmlRootElement
public class Pager<T> {
    private List<T> items;
    private Long totalItems;
    private Integer page;
    private Integer itemsPerPage;

    public Pager() {
    }

    public Pager(List<T> items, Long totalItems, Integer page, Integer itemsPerPage) {
        this.items = items;
        this.totalItems = totalItems;
        this.page = page;
        this.setItemsPerPage(itemsPerPage);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(Integer itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }


}
