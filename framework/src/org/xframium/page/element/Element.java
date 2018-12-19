/*******************************************************************************
 * xFramium
 *
 * Copyright 2016 by Moreland Labs, Ltd. (http://www.morelandlabs.com)
 *
 * Some open source application is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 *  
 * Some open source application is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with xFramium.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *******************************************************************************/
package org.xframium.page.element;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.xframium.device.factory.DeviceWebDriver;
import org.xframium.gesture.Gesture.GestureType;
import org.xframium.integrations.perfectoMobile.rest.services.Imaging.Resolution;
import org.xframium.page.BY;
import org.xframium.reporting.ExecutionContextTest;


// TODO: Auto-generated Javadoc
/**
 * The Interface Element.
 */
public interface Element 
{
  public static Pattern TOKEN_REGEX = Pattern.compile( "\\{([^\\}]*)\\}" );
	/** The Constant CONTEXT_ELEMENT. */
	public static final String CONTEXT_ELEMENT = "_CONTEXT.ELEMENT";
	
	/** The Constant CONTEXT_INDEX. */
	public static final String CONTEXT_INDEX = "_CONTEXT.ELEMENT.INDEX";
	
	/** The Constant CONTEXT_INDEX. */
    public static final String LOOP_INDEX = "_LOOP.INDEX";
    
    public enum SetMethod
    {
        DEFAULT,
        SINGLE,
    	MULTISELECT,
    	VALIDATE,
    	BY_VALUE,
    	BY_VISIBLE_TEXT,
    	DELAYED,
    	PERFECTO,
    	UP_FIRST,
    	DOWN_FIRST,
    	KEYS;
    }
	
	/**
	 * The Enum WAIT_FOR.
	 */
	public enum WAIT_FOR
    {
        
        /** The clickable. */
        CLICKABLE( 1, "CLICKABLE", "Clickable"),
        
        /** The selectable. */
        SELECTABLE( 2, "SELECTABLE", "Selectable"),
        
        /** The invisible. */
        INVISIBLE( 3, "INVISIBLE", "Invisible"),
        
        /** The present. */
        PRESENT( 4, "PRESENT", "Present"),
        
        /** The text present. */
        TEXT_PRESENT( 5, "TEXT_PRESENT", "Text Present"),
        
        /** The text value present. */
        TEXT_VALUE_PRESENT( 6, "TEXT_VALUE_PRESENT", "Text Value Present"),
        
        /** The title contains. */
        TITLE_CONTAINS( 7, "TITLE_CONTAINS", "Title Contains"),
        
        /** The title is. */
        TITLE_IS( 8, "TITLE_IS", "Title Equals"),
        
        /** The visible. */
        VISIBLE( 9, "VISIBLE", "Visible"),
        FRAME( 10, "FRAME", "Waits for a frame and switches to it"),
        
        CLICKABLE_THEN_CLICK( 11, "CLICKABLE_THEN_CLICK", "Waits for Clickable then clicks on the element"),
        
        WINDOW( 12, "WINDOW", "Waits for the window to be available then switches to it"),
        
        ENABLED_THEN_SET (13, "ENABLED_THEN_SET", "Waits for an element to be in an enabled state then attempts to set the value" )
        ;
	    
	    private WAIT_FOR( int id, String name, String description )
        {
            this.id = id;
            this.name= name;
            this.description = description;
        }
        
        private int id;
        private String name;
        private String description;
        
        public List<WAIT_FOR> getSupported()
        {
            List<WAIT_FOR> wList = new ArrayList<WAIT_FOR>( 10 );
            wList.add( WAIT_FOR.CLICKABLE );
            wList.add( WAIT_FOR.SELECTABLE );
            wList.add( WAIT_FOR.INVISIBLE );
            wList.add( WAIT_FOR.PRESENT );
            wList.add( WAIT_FOR.TEXT_PRESENT );
            wList.add( WAIT_FOR.TEXT_VALUE_PRESENT );
            wList.add( WAIT_FOR.TITLE_CONTAINS );
            wList.add( WAIT_FOR.TITLE_IS );
            wList.add( WAIT_FOR.VISIBLE );
            wList.add( WAIT_FOR.FRAME );
            wList.add( WAIT_FOR.CLICKABLE_THEN_CLICK );
            wList.add( WAIT_FOR.WINDOW );
            wList.add( WAIT_FOR.ENABLED_THEN_SET );
            return wList;
        }
    }
	
	public void setExecutionContext( ExecutionContextTest executionContext );
    public ExecutionContextTest getExecutionContext();
	
	public String getElementName();
    public String getPageName();
	
	public String getClassification();
    public void setClassification( String classification );
	
	public void addElementProperty( String name, String value );
    public Map<String,String> getElementProperties();
    public String getElementProperty( String name );
    public void addSubElement( SubElement subElement );
    public DeviceWebDriver getWebDriver();
    
    public boolean isAllowMultiple();
	public void setAllowMultiple(boolean allowMultiple); 
	
	public BY getBy();
	
	/**
	 * Move to.
	 *
	 * @return true, if successful
	 */
	public boolean moveTo();
	
	public boolean clickAt( int offsetPercentX, int offsetPercentY );

    /**
     * Get Element Key
     *
     * @return String
     */
    public String getKey();
	
	/**
	 * Press.
	 *
	 * @return true, if successful
	 */
	public boolean press();
	
	public boolean isSelected();
	
	/**
	 * Release.
	 *
	 * @return true, if successful
	 */
	public boolean release();
	
	/**
	 * Gets the native.
	 *
	 * @return the native
	 */
	public Object getNative();
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue();
	
	/**
	 * Gets the style.
	 *
	 * @param styleProperty the style property
	 * @return the style
	 */
	public String getStyle( String styleProperty );
	
	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	public boolean isVisible();
	
	/**
	 * Checks if is present.
	 *
	 * @return true, if is present
	 */
	public boolean isPresent();
	
	public Dimension getSize();
	
	public Point getAt();
	
	public boolean isFocused();
	
	public boolean isEnabled();
	/**
	 * Wait for visible.
	 *
	 * @param timeOut the time out
	 * @param timeUnit the time unit
	 * @return true, if successful
	 */
	public boolean waitForVisible( long timeOut, TimeUnit timeUnit  );
	
	/**
	 * Wait for.
	 *
	 * @param timeout the timeout
	 * @param timeUnit the time unit
	 * @param waitType the wait type
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean waitFor( long timeout, TimeUnit timeUnit, WAIT_FOR waitType, String value );
	
	/**
	 * Wait for present.
	 *
	 * @param timeOut the time out
	 * @param timeUnit the time unit
	 * @return true, if successful
	 */
	public boolean waitForPresent( long timeOut, TimeUnit timeUnit  );
	
	/**
	 * Gets the attribute.
	 *
	 * @param attributeName the attribute name
	 * @return the attribute
	 */
	public String getAttribute( String attributeName );
	
	/**
	 * Sets the value.
	 *
	 * @param currentValue the new value
	 */
	public void setValue( String currentValue, String xFID );
	
	public void setValue( String currentValue, SetMethod setMethod, String xFID );
	
	/**
	 * Sets the driver.
	 *
	 * @param webDriver the new driver
	 */
	public void setDriver( Object webDriver );
	
	/**
	 * Sets the timed.
	 *
	 * @param timed the new timed
	 */
	public void setTimed( boolean timed );
	
	/**
	 * Checks if is timed.
	 *
	 * @return true, if is timed
	 */
	public boolean isTimed();

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	public Element[] getAll();
	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public int getIndex();
	
	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public int getCount();
	
	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public Element getContext();
	public String getRawKey();
	
	/**
	 * Sets the context.
	 *
	 * @param context the new context
	 */
	public void setContext( Element context );
	
	public void setDeviceContext( String deviceContext );
	public String getDeviceContext();
	
	
	/**
	 * The Enum TAG.
	 */
	public enum TAG
	{
		
		/** The input. */
		INPUT ( "input" ),
		
		/** The img. */
		IMG ( "img" );
		
		/** The tag name. */
		private String tagName;
		
		/**
		 * Instantiates a new tag.
		 *
		 * @param tagName the tag name
		 */
		TAG( String tagName )
		{
			this.tagName = tagName;
		}
		
	}
	
	/**
	 * Gets the execution id.
	 *
	 * @return the execution id
	 */
	public String getExecutionId();
	
	/**
	 * Gets the device name.
	 *
	 * @return the device name
	 */
	public String getDeviceName();
	
	/**
	 * Adds the token.
	 *
	 * @param tokenName the token name
	 * @param tokenValue the token value
	 */
	public Element addToken( String tokenName, String tokenValue );
	
	/**
	 * Adds the token.
	 *
	 * @param tokenPairValue the token pair value
	 */
	public Element addToken( String tokenPairValue );
	
	/**
	 * Clone element.
	 *
	 * @return the element
	 */
	public Element cloneElement();
	
	
	/**
	 * Locates an element and extracts it as an image given the element 
	 * had specified coordinates and size.
	 *
	 * @param imageResolution the image resolution
	 * @return the element
	 */
	public Image getImage( Resolution imageResolution );
	
	public void setCacheNative( boolean cacheNative );
	public boolean isCacheNative();
	/**
	 * Click.
	 */
	public void click();
	
	public void click (int clicks, int waitTime);
	
	public boolean clickFor( int lengthInMillis );
	
	public String getName();
}
