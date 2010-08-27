/**
 *
 * Copyright (c) 2009-2010
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.ejb.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is Keywords entity.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Entity
@Table(name = "TOPCAT_KEYWORDS")
@NamedQueries({
    @NamedQuery(name = "TopcatKeywords.findAll", query = "SELECT t FROM TopcatKeywords t"),
    @NamedQuery(name = "TopcatKeywords.findByKeyword", query = "SELECT t FROM TopcatKeywords t WHERE t.keyword = :keyword"),
    //@NamedQuery(name = "TopcatKeywords.findByPrefix", query = "SELECT t FROM TopcatKeywords t WHERE LOWER(t.keyword) LIKE :prefix ORDER BY t.keyword")
    @NamedQuery(name = "TopcatKeywords.findByPrefix", query = "SELECT t FROM TopcatKeywords t WHERE t.keyword LIKE :prefix ORDER BY t.keyword")
})
@XmlRootElement
public class TopcatKeywords implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "KEYWORD")
    private String keyword;

    public TopcatKeywords() {
    }

    public TopcatKeywords(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (keyword != null ? keyword.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TopcatKeywords)) {
            return false;
        }
        TopcatKeywords other = (TopcatKeywords) object;
        if ((this.keyword == null && other.keyword != null) || (this.keyword != null && !this.keyword.equals(other.keyword))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatKeywords[keyword=" + keyword + "]";
    }

}
