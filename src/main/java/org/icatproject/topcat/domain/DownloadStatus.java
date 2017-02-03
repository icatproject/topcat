package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public enum DownloadStatus {
    RESTORING, COMPLETE, EXPIRED, PAUSED, PREPARING
}
