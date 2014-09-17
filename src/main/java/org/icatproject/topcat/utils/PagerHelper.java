package org.icatproject.topcat.utils;

import java.lang.reflect.Field;
import java.util.Date;

import org.icatproject.topcat.domain.SortOrder;
import org.icatproject.topcat.domain.TInvestigation;

/**
 * PagerHelper class to work out offset and check if the specified
 * sort field is valid by using reflection
 *
 */
public class PagerHelper{
    private Integer page;
    private Integer maxPerPage;
    @SuppressWarnings("rawtypes")
    private Class cls;
    private String sort;
    private String order;
    private Integer offset = 0;
    private String sortOption = "id";
    private SortOrder orderOption = SortOrder.ASC;

    /**
     * Constructor where field and order can be specified
     *
     * @param page the page number
     * @param maxPerPage max items per page
     * @param cls the class
     * @param sort the field to sort
     * @param order the order asc or desc
     */
    @SuppressWarnings("rawtypes")
    public PagerHelper(Integer page, Integer maxPerPage, Class cls,
            String sort, String order) {
        this.page = page;
        this.maxPerPage = maxPerPage;
        this.cls = cls;
        this.sort = sort;
        this.order = order;

        init();
    }

    /**
     * Constructor using id and asc as default sorting
     *
     * @param page the page number
     * @param maxPerPage maxPerPage max items per page
     * @param cls cls the class
     */
    @SuppressWarnings("rawtypes")
    public PagerHelper(Integer page, Integer maxPerPage, Class cls) {
        this.page = page;
        this.maxPerPage = maxPerPage;
        this.cls = cls;

        init();
    }


    private void init() throws IllegalArgumentException {
        if (page > 0) {
            offset = (page - 1) * maxPerPage;
        }

        if (sort != null) {
            boolean isValid = isFieldValid(TInvestigation.class, sort);

            if (! isValid) {
                throw new IllegalArgumentException(sort + " is not a valid sort field");
            }

            sortOption = sort;
        }

        if (order != null) {
            if (order.equalsIgnoreCase("ASC")){
                orderOption = SortOrder.ASC;
            } else if (order.equalsIgnoreCase("DESC")){
                orderOption = SortOrder.DESC;
            } else {
                throw new IllegalArgumentException("order option must be asc or desc");
            }
        }
    }


    /**
    *
    * @param class the class
    * @param fieldName the name of the field
    * @return whether the field is valid for sorting
    */
   @SuppressWarnings("rawtypes")
   private boolean isFieldValid(Class cls, String fieldName) {
       //final Logger logger = Logger.getLogger(SortFieldChecker.class);
       Field field = null;

       try {
           field = cls.getDeclaredField(fieldName);
       } catch (NoSuchFieldException | SecurityException e) {
           return false;
       }

       if (field != null) {
           //Allow only sorting of type Long, String, Date, Double, boolean and Integer. Meaningless to sort collections
           if (field.getType() != Long.class && field.getType() != String.class &&
                   field.getType() != Date.class && field.getType() != Double.class &&
                   field.getType() != boolean.class && field.getType() != Integer.class) {
               return false;
           }
       }

       return true;

   }


    public Integer getPage() {
        return page;
    }

    public Integer getMaxPerPage() {
        return maxPerPage;
    }

    public void setMaxPerPage(Integer maxPerPage) {
        this.maxPerPage = maxPerPage;
    }

    @SuppressWarnings("rawtypes")
    public Class getCls() {
        return cls;
    }

    public Integer getOffset() {
        return offset;
    }

    public String getSortOption() {
        return sortOption;
    }

    public SortOrder getOrderOption() {
        return orderOption;
    }
}
