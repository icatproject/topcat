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
package uk.ac.stfc.topcat.gwt.client.images.icons;
/**
 * Imports
 */
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * This class is a resource to hold icons that need to be displayed in the interface.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public interface IconResource extends ClientBundle  {
	  @Source ("login-icon.png")
	  ImageResource iconLogin();

	  @Source  ("wait-icon.gif")
	  ImageResource iconWait();

	  @Source ("warn-icon.jpg")
	  ImageResource iconWarn();

	  @Source ("download.png")
	  ImageResource iconDownload();

	  @Source ("folder_add.png")
      ImageResource iconAddDataset();

	  @Source ("page_white_add.png")
      ImageResource iconAddDatafile();

	  @Source ("folder_table.png")
	  ImageResource iconOpenDataset();

	  @Source ("folder_page_white.png")
      ImageResource iconOpenDatafile();

	  @Source ("folder_explore.png")
      ImageResource iconShowDatasetParameter();

	  @Source ("page_white_put.png")
      ImageResource iconDownloadDatafile();

	  @Source ("folder_down.png")
      ImageResource iconDownloadDataset();

	  @Source ("box_down.png")
      ImageResource iconDownloadInvestigation();

	  @Source ("page_white_magnify.png")
      ImageResource iconShowDatafileParameter();

	  @Source ("zoom.png")
      ImageResource iconShowInvestigationDetails();

	  @Source ("information.png")
      ImageResource iconInformation();

	  @Source ("chart_bar.png")
      ImageResource iconFileSize();

	  @Source ("arrow_in.png")
	  ImageResource collapseAll();

      @Source ("arrow_rotate_clockwise.png")
      ImageResource clearAll();

}
