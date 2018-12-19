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
package org.xframium.page.keyWord.step;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.xframium.application.ApplicationVersion;
import org.xframium.artifact.ArtifactManager;
import org.xframium.artifact.ArtifactType;
import org.xframium.container.SuiteContainer;
import org.xframium.content.ContentManager;
import org.xframium.device.ConnectedDevice;
import org.xframium.device.data.DataManager;
import org.xframium.device.factory.DeviceWebDriver;
import org.xframium.exception.BubbledFailureException;
import org.xframium.exception.FilteredException;
import org.xframium.exception.FlowException;
import org.xframium.exception.ObjectConfigurationException;
import org.xframium.exception.ScriptConfigurationException;
import org.xframium.exception.ScriptException;
import org.xframium.gesture.Gesture.Direction;
import org.xframium.gesture.GestureManager;
import org.xframium.integrations.perfectoMobile.rest.PerfectoMobile;
import org.xframium.integrations.perfectoMobile.rest.services.WindTunnel.Status;
import org.xframium.page.ElementDescriptor;
import org.xframium.page.Page;
import org.xframium.page.PageManager;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.element.Element;
import org.xframium.page.keyWord.KeyWordDriver;
import org.xframium.page.keyWord.KeyWordDriver.TRACE;
import org.xframium.page.keyWord.KeyWordParameter;
import org.xframium.page.keyWord.KeyWordStep;
import org.xframium.page.keyWord.KeyWordToken;
import org.xframium.page.keyWord.step.spi.KWSElse;
import org.xframium.page.keyWord.step.spi.KWSLoopBreak;
import org.xframium.page.keyWord.step.transform.ValueTransformationFactory;
import org.xframium.reporting.ExecutionContext;
import org.xframium.reporting.ExecutionContextStep;
import org.xframium.reporting.ExecutionContextTest;
import org.xframium.spi.driver.ReportiumProvider;
import org.xframium.utility.ImageUtility;
import org.xframium.utility.XMLEscape;

import cucumber.api.DataTable;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractKeyWordStep.
 */
public abstract class AbstractKeyWordStep implements KeyWordStep
{
    private static final Map<String,String> HASH_CACHE = new HashMap<String,String>(20);
    private static final String[] WEB_PERFORMANCE = new String[] { "navigationStart", "redirectStart", "redirectEnd", "fetchStart", "domainLookupStart", "domainLookupEnd", "connectStart", "connectEnd", "requestStart", "responseStart", "responseEnd", "unloadStart", "unloadEnd", "domLoading", "domInteractive", "domContentLoaded", "domComplete", "loadEventStart", "loadEventEnd" };
    private static NumberFormat numberFormat = new DecimalFormat( "0000" );
    
    private static Pattern DATA_PATTERN = Pattern.compile( "data\\{(\\w*)\\}" );

    public AbstractKeyWordStep()
    {
        kwImpl = getClass().getName();
        kw = KeyWordStepFactory.instance().getKW( getClass() );
        natualLanguage = PageManager.getFormattedMessage( getClass().getSimpleName() );
        category = "Other";
    }

    protected int featureId;
    
	  private String waitFor = null;
    /** The name. */
    private String name;

    /** The page name. */
    private String pageName;

    private String siteName;

    /** The active. */
    private boolean active;

    /** The link id. */
    private String linkId;

    /** The timed. */
    private boolean timed;

    private boolean startAt;

    private boolean breakpoint;
    
    private String appContext;

    protected String category;

    protected boolean orMapping = true;

    private static Random numberGenerator = new Random();

    /** The s failure. */
    private StepFailure sFailure;

    /** The inverse. */
    private boolean inverse = false;
    
    private boolean trace = false;

    /** The Constant SPLIT. */
    private static final String SPLIT = "-->";

    /** The fork. */
    private boolean fork;

    /** The os. */
    private String os;

    /** The browser. */
    private String browser;

    /** The context. */
    private String context;

    /** The validation. */
    private String validation;

    /** The threshold. */
    private int threshold;

    /** The description. */
    private String description;

    /** The validation type. */
    private ValidationType validationType;

    /** The poi. */
    private String poi;

    /** The wait time. */
    private long waitTime;

    /** The device. */
    private String device;

    private String[] tagNames;
    private String[] deviceTags;

    protected String kwName = "N/A";
    protected String kwDescription;
    protected String kwHelp;
    protected String kwImpl;
    protected String kw;
    protected String natualLanguage;
    protected ApplicationVersion version;
    
    private String image;
    
    private String successReport;
    private String failureReport;
    private List<KeyWordToken> reportingTokens = new ArrayList<KeyWordToken>( 10 );
    private boolean allowMultiple = false;
    private DataTable dataTable;
    
    
   
  private static Pattern TOKEN_REGEX = Pattern.compile( "\\{([^\\}]*)\\}" );

	protected String getReportMessage( StepStatus stepStatus, Map<String,Object> contextMap, Map<String, PageData> dataMap, String xFID )
    {
    	String returnValue = null;
    	
    	 switch( stepStatus )
    	 {
    	 	case FAILURE:
    	 	case FAILURE_IGNORED:
    	 		returnValue = failureReport;
    	 		break;
    	 		
    	 	case SUCCESS:
    	 		returnValue = successReport;
    	 		break;
    	 		
    	 	default:
    	 		returnValue = null;
    	 		break;
    	 }
    	 
    	if ( returnValue != null && reportingTokens != null && reportingTokens.size() > 0 )
        {
            for ( KeyWordToken token : reportingTokens )
            {
                if ( log.isDebugEnabled() )
                    log.debug( "Applying token " + token.getName() );

                returnValue = ( returnValue + "").replace( "{" + token.getName() + "}", getTokenValue( token, contextMap, dataMap, xFID ) );
            }
        }
    	
    	try
    	{
      	boolean valueReplaced = false;
      	do
      	{
      	  valueReplaced = false;
        	Matcher m = TOKEN_REGEX.matcher( returnValue );
        	while( m.find() )
        	{
        	  String propertyValue = KeyWordDriver.instance( xFID ).getProperty( m.group(1) );
        	  if ( propertyValue != null )
        	  {
        	    returnValue = returnValue.replace( "{" + m.group( 1 ) + "}", propertyValue );
        	    valueReplaced = true;
        	    break;
        	  }
        	}
      	}
      	while( valueReplaced );
    	}
    	catch( Exception e )
    	{
    	  
    	}
    	
    	return returnValue;
    }
    
	 public DataTable getDataTable() {
		return dataTable;
	}


	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}

    public boolean isAllowMultiple() {
		return allowMultiple;
	}



	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}



	public String getSuccessReport() {
		return successReport;
	}



	public void setSuccessReport(String successReport) {
		this.successReport = successReport;
	}



	public String getFailureReport() {
		return failureReport;
	}



	public void setFailureReport(String failureReport) {
		this.failureReport = failureReport;
	}
	
	@Override
	public List<KeyWordToken> getReportTokenList() {
		return reportingTokens;
	}



	public boolean isTrace()
    {
        return trace;
    }

    public void setTrace(boolean trace)
    {
        this.trace = trace;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }
    
    @Override
    public void addReportingToken(KeyWordToken t) {
    	if ( reportingTokens == null )
    		reportingTokens = new ArrayList<KeyWordToken>( 5 );
    	
    	reportingTokens.add( t );
    	
    }

    protected void addContext( String contextName, Object contextValue, Map<String,Object> contextMap, ExecutionContextTest eC )
    {
        
        Map<String,Object> cP = (Map<String,Object>) eC.getStep().getExecutionParameter().get( "CONTEXT" );
        if ( cP == null )
        {
            cP = new HashMap<String,Object>( 10 );
            eC.getStep().getExecutionParameter().put( "CONTEXT", cP );
        }
        
        cP.put( contextName, contextValue );
        contextMap.put( contextName, contextValue );
    }
    
    protected boolean validateParameters( String[] parameterNames )
    {
        for ( String pName : parameterNames )
        {
            if ( getParameter( pName ) == null )
            {
                String params = "";
                for ( String pName2 : parameterNames )
                    params += pName2 + ", ";
                throw new ScriptConfigurationException( "The following parameters must be specified for " + kw + " [" + params + "]" );
            }
        }
        
        return true;
    }
    
    
    
    public String getWaitFor()
    {
        return waitFor;
    }

    public void setWaitFor(String waitFor)
    {
        this.waitFor = waitFor;
    }

    public String getAppContext()
    {
        return appContext;
    }

    public void setAppContext( String appContext )
    {
        this.appContext = appContext;
    }

    public boolean isOrMapping()
    {
        return orMapping;
    }

    @Override
    public boolean isBreakpoint()
    {
        return breakpoint;
    }

    @Override
    public ApplicationVersion getVersion()
    {
        return version;
    }
    
    @Override
    public void setVersion( String appVersion )
    {
        if ( appVersion != null )
            version = new ApplicationVersion( appVersion );
    }
    
    @Override
    public void setBreakpoint( boolean breakpoint )
    {
        this.breakpoint = breakpoint;
    }

    @Override
    public String getKw()
    {
        // TODO Auto-generated method stub
        return kw;
    }

    protected void scroll( Direction scrollDirection, DeviceWebDriver webDriver )
    {
        //
        // This is randomized to ensure that we are not swiping from an area
        // that does not allow a swipe
        //
        int randomNumber = numberGenerator.nextInt( 45 );

        switch ( scrollDirection )
        {
            case DOWN:
                scroll( new Point( 50, randomNumber + 10 ), new Point( 50, 45 + randomNumber ), webDriver );
                break;

            case LEFT:
                scroll( new Point( randomNumber + 5, 50 ), new Point( 45 + randomNumber, 50 ), webDriver );
                break;

            case RIGHT:
                scroll( new Point( 45 + randomNumber, 50 ), new Point( randomNumber, 50 ), webDriver );
                break;

            case UP:
                scroll( new Point( 50, 45 + randomNumber ), new Point( 50, randomNumber ), webDriver );
                break;
        }
    }
    
    protected void scroll( Point fromPoint, Point toPoint, DeviceWebDriver webDriver )
    {
        GestureManager.instance( webDriver ).createSwipe( webDriver.getExecutionContext().getxFID(), fromPoint, toPoint ).executeGesture( webDriver );
    }

    public String[] getTagNames()
    {
        return this.tagNames;
    }
    
    public void setTagNames( String[] tagNames )
    {
        this.tagNames = tagNames;
    }
    
    public void setDeviceTags( String[] deviceTags )
    {
        this.deviceTags = deviceTags;
    }
    
    @Override
    public void setTagNames( String tagNames )
    {
        if ( tagNames != null && !tagNames.isEmpty() )
        {
            this.tagNames = tagNames.split( "," );
        }
    }

    public String[] getDeviceTags()
    {
        return this.deviceTags;
    }
    
    public void setDeviceTags( String deviceTags )
    {
        if ( deviceTags != null && !deviceTags.isEmpty() )
        {
            this.deviceTags = deviceTags.split( "," );
        }
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName( String siteName )
    {
        this.siteName = siteName;
    }

    public boolean isStartAt()
    {
        return startAt;
    }

    public void setStartAt( boolean startAt )
    {
        this.startAt = startAt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getPoi()
     */
    public String getPoi()
    {
        return poi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setPoi(java.lang.String)
     */
    public void setPoi( String poi )
    {
        this.poi = poi;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getContext()
     */
    public String getContext()
    {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setContext(java.lang.String)
     */
    public void setContext( String context )
    {
        this.context = context;
    }

    /** The parameter list. */
    private List<KeyWordParameter> parameterList = new ArrayList<KeyWordParameter>( 10 );

    /** The token list. */
    private List<KeyWordToken> tokenList = new ArrayList<KeyWordToken>( 10 );

    /** The log. */
    protected Log log = LogFactory.getLog( AbstractKeyWordStep.class );

    /**
     * _execute step.
     *
     * @param pageObject
     *            the page object
     * @param webDriver
     *            the web driver
     * @param contextMap
     *            the context map
     * @param dataMap
     *            the data map
     * @param pageMap
     *            TODO
     * @param sC
     *            TODO
     * @param executionContext
     *            TODO
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    protected abstract boolean _executeStep( Page pageObject, WebDriver webDriver, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, SuiteContainer sC, ExecutionContextTest executionContext ) throws Exception;

    /**
     * Creates the point.
     *
     * @param pointValue
     *            the point value
     * @return the point
     */
    protected Point createPoint( String pointValue )
    {
        Point x = null;

        try
        {
            String[] coors = pointValue.split( "," );

            if ( coors.length == 2 )
            {
                x = new Point( Integer.parseInt( coors[0].trim() ), Integer.parseInt( coors[1].trim() ) );
                return x;
            }
        }
        catch ( Exception e )
        {
            log.warn( Thread.currentThread().getName() + ": Could not parse coordinates " + pointValue + " due to " + e.getMessage() );
        }

        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#toError()
     */
    public String toError()
    {
        return getClass().getSimpleName() + " returned false";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#isFork()
     */
    public boolean isFork()
    {
        return fork;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getBrowser()
     */
    @Override
    public String getBrowser()
    {
        return browser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setBrowser(java.lang.String)
     */
    @Override
    public void setBrowser( String browser )
    {
        this.browser = browser != null ? browser.toUpperCase() : browser;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getOs()
     */
    @Override
    public String getOs()
    {
        return os;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setOs(java.lang.String)
     */
    @Override
    public void setOs( String os )
    {
        this.os = os != null ? os.toUpperCase() : os;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getThreshold()
     */
    public int getThreshold()
    {
        return threshold;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setThreshold(int)
     */
    public void setThreshold( int threshold )
    {
        this.threshold = threshold;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setDescription(java.lang.
     * String)
     */
    public void setDescription( String description )
    {
        this.description = description;
    }

    /**
     * Checks if is recordable.
     *
     * @return true, if is recordable
     */
    public boolean isRecordable()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setFork(boolean)
     */
    public void setFork( boolean fork )
    {
        this.fork = fork;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#isInverse()
     */
    public boolean isInverse()
    {
        return inverse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setInverse(boolean)
     */
    public void setInverse( boolean inverse )
    {
        this.inverse = inverse;
    }

    /** The step list. */
    private List<KeyWordStep> stepList = new ArrayList<KeyWordStep>( 10 );

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#isTimed()
     */
    public boolean isTimed()
    {
        return timed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setTimed(boolean)
     */
    public void setTimed( boolean timed )
    {
        this.timed = timed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getFailure()
     */
    @Override
    public StepFailure getFailure()
    {
        return sFailure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setFailure(com.perfectoMobile
     * .page.keyWord.KeyWordStep.StepFailure)
     */
    @Override
    public void setFailure( StepFailure sFailure )
    {
        this.sFailure = sFailure;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getDevice()
     */
    public String getDevice()
    {
        return device;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setDevice(java.lang.String)
     */
    public void setDevice( String device )
    {
        this.device = device;
    }

    /**
     * Gets the element.
     *
     * @param pageObject
     *            the page object
     * @param contextMap
     *            the context map
     * @param webDriver
     *            the web driver
     * @param dataMap
     *            the data map
     * @return the element
     */
    protected Element getElement( Page pageObject, Map<String, Object> contextMap, Object webDriver, Map<String, PageData> dataMap, ExecutionContextTest executionContext )
    {
        return getElement( pageObject, contextMap, webDriver, dataMap, null, executionContext );
    }

    
    /**
     * Gets the element.
     *
     * @param pageObject
     *            the page object
     * @param contextMap
     *            the context map
     * @param webDriver
     *            the web driver
     * @param dataMap
     *            the data map
     * @param overrideName
     *            the override name
     * @return the element
     */
    protected Element getElement( Page pageObject, Map<String, Object> contextMap, Object webDriver, Map<String, PageData> dataMap, String overrideName, ExecutionContextTest executionContext )
    {
        String useName = name;
        if ( overrideName != null && !overrideName.isEmpty() )
            useName = overrideName;

        //
        // We need to remove the preceding page and site from CONTEXT_ELEMENT 
        //
        if ( useName.contains( Element.CONTEXT_ELEMENT ) )
            useName = useName.substring( useName.indexOf( Element.CONTEXT_ELEMENT ) );
        
        if ( useName.startsWith( Element.CONTEXT_ELEMENT ) )
        {
            if ( Element.CONTEXT_ELEMENT.equals( useName ) )
            {
                if ( log.isDebugEnabled() )
                    log.debug( Thread.currentThread().getName() + ": Attempting to acquire CONTEXT element" );

                Element currentElement = (Element) contextMap.get( Element.CONTEXT_ELEMENT );
                currentElement = currentElement.cloneElement();
                currentElement.setTimed( isTimed() );
                currentElement.setExecutionContext( executionContext );
                currentElement.setAllowMultiple( isAllowMultiple() );

                if ( log.isDebugEnabled() )
                    log.debug( Thread.currentThread().getName() + ": CONTEXT element found as " + currentElement );

                return currentElement;
            }
            else
            {
                String elementName = useName.split( SPLIT )[1];

                if ( log.isDebugEnabled() )
                    log.debug( Thread.currentThread().getName() + ": Attempting to acquire CONTEXT element" );

                Element currentElement = (Element) contextMap.get( Element.CONTEXT_ELEMENT );

                if ( log.isDebugEnabled() )
                    log.debug( Thread.currentThread().getName() + ": CONTEXT element found as " + currentElement );

                ElementDescriptor elementDescriptor = new ElementDescriptor( siteName != null && siteName.trim().length() > 0 ? siteName : PageManager.instance( executionContext.getxFID() ).getSiteName(), getPageName(), elementName );
                Element originalElement = pageObject.getElement( elementDescriptor );
                if ( originalElement == null )
                    throw new ObjectConfigurationException( siteName != null && siteName.trim().length() > 0 ? siteName : PageManager.instance( executionContext.getxFID() ).getSiteName(), getPageName(), elementName );

                Element myElement = originalElement.cloneElement();
                if ( myElement == null )
                {
                    log.error( Thread.currentThread().getName() + ": **** COULD NOT LOCATE ELEMENT [" + elementDescriptor.toString() + "]  Make sure your Page Name and Element Name are spelled correctly and that they have been defined" );
                    return null;
                }
                
                myElement.setTimed( isTimed() );
                myElement.setDriver( webDriver );
                myElement.setAllowMultiple( isAllowMultiple() );

                for ( KeyWordToken token : tokenList )
                {
                    myElement.addToken( token.getName(), getTokenValue( token, contextMap, dataMap, executionContext.getxFID() ) );
                }

                myElement.setCacheNative( true );

                myElement.setDriver( webDriver );
                myElement.setContext( currentElement );
                myElement.setExecutionContext( executionContext );
                return myElement;
            }
        }
        else
        {
            if ( tokenList != null && !tokenList.isEmpty() )
            {
                if ( log.isInfoEnabled() )
                    log.info( Thread.currentThread().getName() + ": Cloning Element " + useName + " on page " + pageName );

                ElementDescriptor elementDescriptor = new ElementDescriptor( siteName != null && siteName.trim().length() > 0 ? siteName : PageManager.instance( executionContext.getxFID() ).getSiteName(), pageName, useName );
                
                Element originalElement = pageObject.getElement( elementDescriptor );
                if ( originalElement == null )
                    throw new ObjectConfigurationException( siteName != null && siteName.trim().length() > 0 ? siteName : PageManager.instance( executionContext.getxFID() ).getSiteName(), pageName, useName );

                Element clonedElement = originalElement.cloneElement();
                clonedElement.setDriver( webDriver );

                for ( KeyWordToken token : tokenList )
                {
                    clonedElement.addToken( token.getName(), getTokenValue( token, contextMap, dataMap, executionContext.getxFID() ) );
                }

                clonedElement.setAllowMultiple( isAllowMultiple() );
                clonedElement.setCacheNative( true );
                clonedElement.setExecutionContext( executionContext );
                clonedElement.setTimed( isTimed() );
                return clonedElement;
            }
            else
            {
                try
                {
                    ElementDescriptor elementDescriptor = new ElementDescriptor( siteName != null && siteName.trim().length() > 0 ? siteName : PageManager.instance( executionContext.getxFID() ).getSiteName(), pageName, useName );
                    Element elt = pageObject.getElement( elementDescriptor ).cloneElement();
                    elt.setDriver( webDriver );
                    elt.setCacheNative( true );
                    elt.setExecutionContext( executionContext );
                    elt.setTimed( isTimed() );
                    elt.setAllowMultiple( isAllowMultiple() );
                    return elt;
                }
                catch ( NullPointerException e )
                {
                    throw new ObjectConfigurationException( siteName == null ? PageManager.instance( executionContext.getxFID() ).getSiteName() : siteName, pageName, useName );
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#executeStep(com.
     * perfectoMobile.page.Page, org.openqa.selenium.WebDriver, java.util.Map,
     * java.util.Map)
     */
    public boolean executeStep( Page pageObject, WebDriver webDriver, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, SuiteContainer sC, ExecutionContextTest executionContext ) throws Exception
    {
        long startTime = System.currentTimeMillis();
        boolean returnValue = false;

        WebDriver alternateWebDriver = null;
        try
        {
            Exception stepException = null;
            
        
            

            try
            {
                executionContext.startStep( this, contextMap, dataMap );
                //
                // OS Checks to allow for elements to be skipped if the OS did not
                // match
                //
                if ( os != null )
                {
                    String deviceOs = ( (DeviceWebDriver) webDriver ).getPopulatedDevice().getOs();
                    if ( deviceOs == null )
                    {
                        throw new FilteredException( "A Required OS of [" + os + "] was specified however the OS of the device could not be determined" );
                    }
    
                    String[] osArray = os.split( "," );
                    boolean osFound = false;
                    for ( String localOs : osArray )
                    {
                        if ( localOs.toUpperCase().trim().equals( deviceOs.toUpperCase() ) )
                        {
                            osFound = true;
                            break;
                        }
                    }
    
                    if ( !osFound )
                    {
                        throw new FilteredException( "A Required OS in [" + os + "] was specified however the OS of the device was [" + deviceOs.toUpperCase() + "]" );
                    }
                }
    
                if ( browser != null )
                {
                    String browserName = ((DeviceWebDriver) webDriver).getDevice().getBrowserName();
                    if ( browserName == null )
                    {
                        throw new FilteredException( "A Required Browser of [" + browser + "] was specified however the Browser of the device could not be determined" );
                    }
    
                    String[] browserArray = browser.split( "," );
                    boolean browserFound = false;
                    for ( String localBrowser : browserArray )
                    {
                        if ( localBrowser.toUpperCase().trim().equals( browserName.toUpperCase() ) )
                        {
                            browserFound = true;
                            break;
                        }
                    }
    
                    if ( !browserFound )
                    {
                        throw new FilteredException( "A Required Browser in [" + browser + "] was specified however the Browser of the device was [" + browserName.toUpperCase() + "]" );
                    }
                }
    
                //
                // Device tagging implementation
                //
                
                if ( deviceTags != null && deviceTags.length > 0 )
                {
                    boolean tagFound = false;
                    for ( String localTag : deviceTags )
                    {
                        if ( ( (DeviceWebDriver) webDriver ).getDevice().getTagNames() != null && ( (DeviceWebDriver) webDriver ).getDevice().getTagNames().length > 0 )
                        {
                            for ( String deviceTag : ( (DeviceWebDriver) webDriver ).getDevice().getTagNames() )
                            {
                                if ( localTag.toUpperCase().trim().equals( deviceTag.toUpperCase() ) )
                                {
                                    tagFound = true;
                                    break;
                                }
                            }
                            if ( tagFound )
                                break;
                        }
                        else
                            break;
                    }
    
                    if ( !tagFound )
                    {
                        throw new FilteredException( "This step was ignored as the tag was not specified" );
                    }
                }
    
                //
                // Check for tag names
                //
                if ( tagNames != null && tagNames.length > 0 && PageManager.instance( executionContext.getxFID() ).getTagNames() != null && PageManager.instance( executionContext.getxFID() ).getTagNames().length > 0 )
                {
                    boolean tagEnabled = false;
                    for ( String tagName : tagNames )
                    {
                        for ( String useTag : PageManager.instance( executionContext.getxFID() ).getTagNames() )
                        {
                            if ( tagName.equals( useTag ) )
                            {
                                tagEnabled = true;
                                break;
                            }
                        }
    
                        if ( tagEnabled )
                            break;
                    }
    
                    if ( !tagEnabled )
                    {
                        throw new FilteredException( "This required a tag that was not enabled for this test run" );
                    }
                }
                
                //
                // Validate the version
                //
                if ( version != null )
                {
                    if ( !version.isVersion( ((DeviceWebDriver) webDriver).getAut() ) )
                    {
                        throw new FilteredException( "This required an application version of " + version.toString() );
                    }
                }
                
                
                //
                // Validate the applicationcontext
                //
                if ( appContext != null && !appContext.isEmpty() )
                {
                    if ( !( (DeviceWebDriver) webDriver ).getContext().contains( appContext ) )
                        throw new FilteredException( "This required an application context containing " + appContext.toString() );
                }
    
                if ( log.isInfoEnabled() )
                    log.info( Thread.currentThread().getName() + ": Executing Step " + name + "(" + getClass().getSimpleName() + ")" + (linkId != null ? " linked to " + linkId : "") );
    
                
                if ( getDevice() != null && !getDevice().trim().isEmpty() )
                {
                    //
                    // Use an alternate device for this step
                    //
                    ConnectedDevice alternateDevice = executionContext.getDeviceMap().get( getDevice() );
                    if ( alternateDevice != null )
                        alternateWebDriver = alternateDevice.getWebDriver();
                    else
                        throw new ScriptConfigurationException( "The device [" + getDevice()  + "] was referenced but does not exist or has not been added using an ADD_DEVICE command" );
                }
                else
                {
                    //
                    // A device is not specified, see if one exists in the stack from a CALL
                    //
                    if ( executionContext.peekAtDevice() != null )
                    {
                        ConnectedDevice alternateDevice = executionContext.getDeviceMap().get( executionContext.peekAtDevice() );
                        if ( alternateDevice != null )
                            alternateWebDriver = alternateDevice.getWebDriver();
                        else
                            throw new ScriptConfigurationException( "The device [" + executionContext.peekAtDevice() + "] was referenced but does not exist or has not been added using an ADD_DEVICE command" );
                    }
                }
                
                
                 
                //
                // Listener integrations for individual steps
                //
                if ( !KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).notifyBeforeStep( alternateWebDriver != null ? alternateWebDriver : webDriver, this, pageObject, contextMap, dataMap, pageMap, sC, executionContext ) )
                {
                    throw new FilteredException( "Test Step was skipped due to a failed step notification listener" );
                }

                if ( ((DeviceWebDriver) webDriver).getCloud().getProvider().equals( "PERFECTO" ) )
                {
                    if ( ArtifactManager.instance(( (DeviceWebDriver) webDriver ).getxFID()).isArtifactEnabled( ArtifactType.REPORTIUM.name() ) )
                    {
                        if ( ((ReportiumProvider) webDriver).getReportiumClient() != null )
                        {
                            ((ReportiumProvider) webDriver).getReportiumClient().stepStart( getPageName() + "." + getName() + " (" + getClass().getSimpleName() + ")" );
                        }
                    }
                }

                //
                // Step EXECUTION
                //
                try
                {
                  if ( featureId > 0 )
                    executionContext.addFeature( featureId );
                  
                  returnValue = _executeStep( pageObject, ((alternateWebDriver != null) ? alternateWebDriver : webDriver), contextMap, dataMap, pageMap, sC, executionContext );
                }
                catch( BubbledFailureException be )
                {
                	if ( !inverse )
                		throw be;
                }
                finally
                {
                    if ( ((DeviceWebDriver) webDriver).getCloud().getProvider().equals( "PERFECTO" ) )
                    {
                        if ( ArtifactManager.instance(( (DeviceWebDriver) webDriver ).getxFID()).isArtifactEnabled( ArtifactType.REPORTIUM.name() ) )
                        {
                            if ( ((ReportiumProvider) webDriver).getReportiumClient() != null )
                            {
                                ((ReportiumProvider) webDriver).getReportiumClient().stepEnd( "Finished: " + getClass().getSimpleName() );
                            }
                        }
                    }
                }
                
                //
                // IF a waitFor verification step was specified, execute it
                //
                if ( returnValue && waitFor != null && !waitFor.trim().isEmpty() )
                {
                    String[] pData = waitFor.split("\\.");
                    
                    String eName = null;
                    String pName = null;
                    String sName = null;
                    
                    if ( pData.length >= 3 )
                    {
                        sName = pData[ 0 ];
                        pName = pData[ 1 ];
                        eName = pData[ 2 ];
                    }
                    else if ( pData.length == 2 )
                    {
                        sName = siteName;
                        pName = pData[ 0 ];
                        eName = pData[ 1 ];
                    }
                    else if ( pData.length == 1 )
                    {
                        sName = siteName;
                        pName = pageName;
                        eName = pData[ 0 ];
                    }
                    else
                    {
                        log.error( "waitFor element name specified incorrectly" );
                    }
                        
                    if ( eName != null )
                    {
                        KeyWordStep verifyKeyword = KeyWordStepFactory.instance().createStep( eName, pName, true, "VISIBLE", null, false, StepFailure.ERROR, false, null, null, null, 0, null, 0, null, null, null, null, null, false, false, null, sName, null, null, null, null, true, null, null, false );
                        verifyKeyword.setImage( "VERIFICATION" );
                        if ( !verifyKeyword.executeStep(pageObject, ((alternateWebDriver != null) ? alternateWebDriver : webDriver), contextMap, dataMap, pageMap, sC, executionContext ) )
                            throw new ScriptException( "Failed Verification step" );
                    }
                }
                
                //
                // If threshold was specified then make sure we came in under it
                //
                if ( threshold > 0 )
                {
                    if ( System.currentTimeMillis() - startTime > threshold )
                    {
                        throw new ScriptException( "The current step failed to complete in the defined threshold. Expected[" + threshold + "ms] but it took [" + (System.currentTimeMillis() - startTime) + "ms]" );
                    }
                }

                KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).notifyAfterStep( alternateWebDriver != null ? alternateWebDriver : webDriver, this, pageObject, contextMap, dataMap, pageMap, returnValue ? StepStatus.SUCCESS : StepStatus.FAILURE, sC, executionContext );

            }
            catch ( KWSLoopBreak lb )
            {
                executionContext.completeStep( StepStatus.SUCCESS, null, getReportMessage( StepStatus.SUCCESS, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                throw lb;
            }
            catch ( FlowException lb )
            {
                executionContext.completeStep( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, null, getReportMessage( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                throw lb;
            }
            catch( BubbledFailureException be )
            {
            	executionContext.completeStep( StepStatus.FAILURE, be.getException(), getReportMessage( StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                throw be;
            }
            catch ( FilteredException e )
            {
                stepException = e;
                returnValue = false;
                try
                {
                    KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).notifyAfterStep( alternateWebDriver != null ? alternateWebDriver : webDriver, this, pageObject, contextMap, dataMap, pageMap, StepStatus.SUCCESS, sC, executionContext );
                }
                catch ( Exception e2 )
                {

                }

            }
            catch ( Exception e )
            {
                stepException = new ScriptConfigurationException( e.getMessage() );
                returnValue = false;
                try
                {
                	
                    KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).notifyAfterStep( alternateWebDriver != null ? alternateWebDriver : webDriver, this, pageObject, contextMap, dataMap, pageMap, StepStatus.FAILURE, sC, executionContext );
                }
                catch ( Exception e2 )
                {

                }
                log.debug( Thread.currentThread().getName() + ": ***** Step " + name + " on page " + pageName + " failed ", e );

            }


            if ( inverse )
                returnValue = !returnValue;

            boolean subFailure = false;
            //
            // If there are sub steps and this was successful, then execute
            // those. If we are in fork mode, then skip that execution
            //
            if ( !fork && getStepList() != null && !getStepList().isEmpty() && returnValue )
            {
                boolean subReturnValue = false;
                for ( KeyWordStep step : getStepList() )
                {
                    if ( !step.isActive() )
                        continue;
                    if ( step instanceof KWSElse )
                        continue;
                    try
                    {
                        subReturnValue = step.executeStep( pageObject, webDriver, contextMap, dataMap, pageMap, sC, executionContext );
                    }
                    catch ( KWSLoopBreak e )
                    {
//                        executionContext.completeStep( StepStatus.SUCCESS, null, getReportMessage( StepStatus.SUCCESS, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                        throw e;
                    }
                    catch ( FlowException lb )
                    {
                        executionContext.completeStep( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, null, getReportMessage( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                        throw lb;
                    }
                    catch( BubbledFailureException be )
                    {
                    	executionContext.completeStep( StepStatus.FAILURE, be.getException(), getReportMessage( StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                        throw be;
                    }
                    catch ( Exception e )
                    {
                        stepException = e;
                        subReturnValue = false;
                        log.debug( Thread.currentThread().getName() + ": ***** Step " + name + " on page " + pageName + " encoundered error: ", e );
                    }

                    if ( !subReturnValue )
                    {
                        returnValue = false;
                        subFailure = false;
                        break;
                    }

                }
            }

            //
            // Special case for the ELSE clause if found - we ignore else on a sub failure
            //
            if ( !fork && getStepList() != null && !getStepList().isEmpty() && !returnValue && !subFailure  )
            {
                if ( stepException == null || !(stepException instanceof FilteredException) )
                {
                    for ( KeyWordStep parentStep : getStepList() )
                    {
                        if ( parentStep instanceof KWSElse && !parentStep.getStepList().isEmpty() )
                        {
                            for ( KeyWordStep step : parentStep.getStepList() )
                            {
                                if ( !step.isActive() )
                                    continue;
                                boolean subReturnValue = false;
                                try
                                {
                                    subReturnValue = step.executeStep( pageObject, webDriver, contextMap, dataMap, pageMap, sC, executionContext );
                                }
                                catch ( KWSLoopBreak e )
                                {
                                    executionContext.completeStep( StepStatus.SUCCESS, null, getReportMessage( StepStatus.SUCCESS, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                                    throw e;
                                }
                                catch ( FlowException lb )
                                {
                                    executionContext.completeStep( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, null, getReportMessage( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                                    throw lb;
                                }
                                catch( BubbledFailureException be )
                                {
                                	executionContext.completeStep( StepStatus.FAILURE, be.getException(), getReportMessage( StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
                                    throw be;
                                }
                                catch ( Exception e )
                                {
                                    stepException = e;
                                    subReturnValue = false;
                                    log.debug( Thread.currentThread().getName() + ": ***** Step " + name + " on page " + pageName + " encoundered error: ", e );
                                }
    
                                if ( !subReturnValue )
                                {
                                    returnValue = false;
                                    //if ( step.getFailure().equals( StepStatus.FAILURE_IGNORED ) )
                                        subFailure = false;
                                    //else
                                    //    subFailure = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            StepStatus stepStatus = StepStatus.SUCCESS;

            if ( !returnValue )
            {
                if ( stepException != null && stepException instanceof FilteredException )
                {
                    stepStatus = StepStatus.FILTERED;
                    returnValue = true;
                }
                else
                {
                    switch ( sFailure )
                    {
                        case ERROR:
                            stepStatus = StepStatus.FAILURE;
                            if ( stepException == null )
                                stepException = new ScriptException( kwName + " has failed to complete" );
    
                            break;
    
                        case IGNORE:
                            stepStatus = StepStatus.FAILURE_IGNORED;
                            stepException = null;
                            break;
    
                        case LOG_IGNORE:
                            stepStatus = StepStatus.FAILURE_IGNORED;
                            if ( stepException == null )
                                stepException = new ScriptException( kwName + " has failed to complete" );
                            break;
                         
                        case FAILURE:
                        	stepStatus = StepStatus.FAILURE;
                            if ( stepException == null )
                                stepException = new BubbledFailureException( new ScriptException( kwName + " has failed to complete" ) );
                            else
                            {
                            	if ( !(stepException instanceof BubbledFailureException) )
                            		stepException = new BubbledFailureException( stepException );
                            }
                        
                    }
                }
            }

            executionContext.setEndTime( System.currentTimeMillis() );
            
            if ( isTimed() )
                recordTimings( (DeviceWebDriver)webDriver, executionContext, System.currentTimeMillis(), returnValue ? StepStatus.SUCCESS : StepStatus.FAILURE );
            
            try
            {
                if ( !returnValue )
                {
                    if ( !sFailure.equals( StepFailure.IGNORE ) )
                        dumpState( webDriver, contextMap, dataMap, executionContext );
                }
            }
            catch ( Exception e )
            {
                
            }
            
            //
            // If tracing was enabled then perform a named dump state
            //
            if ( ( isTrace() || ( executionContext.getTest() != null && executionContext.getTest().getTrace() != null && executionContext.getTest().getTrace().equals( TRACE.ON ) ) ) && executionContext.getTest() != null && executionContext.getTest().getTrace() != null && !executionContext.getTest().getTrace().equals( TRACE.DISABLED ) )
            {
                try
                {
                    dumpState( alternateWebDriver != null ? alternateWebDriver : webDriver, contextMap, dataMap, executionContext, getPageName() + "." + getName() );
                }
                catch( Exception e )
                {
                    log.error( "Error acquiring TRACE data", e);
                }
            }
                
            
            executionContext.completeStep( stepStatus, stepException, getReportMessage( stepStatus, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );

            if ( PageManager.instance( executionContext.getxFID() ).isWindTunnelEnabled() && getPoi() != null && !getPoi().isEmpty() )
                PerfectoMobile.instance( ((DeviceWebDriver)webDriver).getxFID()).windTunnel().addPointOfInterest( getExecutionId( webDriver ), getPoi() + "(" + getPageName() + "." + getName() + ")", returnValue ? Status.success : Status.failure );

            if ( !returnValue )
            {
                Throwable currentError = executionContext.getStepException();
                switch ( sFailure )
                {   
                    case ERROR:
                    case FAILURE:
                        returnValue = false;
                        if ( executionContext.getFailedStep() == null )
                            executionContext.setFailedStep( this );
                        
                        if ( currentError == null )
                        {
                            if ( stepException == null )
                                stepException = new ScriptException( toError() );
                            
                            currentError = stepException;

                            if ( isTimed() )
                                recordTimings( (DeviceWebDriver)webDriver, executionContext, System.currentTimeMillis(), StepStatus.FAILURE );

                            if ( PageManager.instance( executionContext.getxFID() ).isWindTunnelEnabled() && getPoi() != null && !getPoi().isEmpty() )
                                PerfectoMobile.instance(((DeviceWebDriver)webDriver).getxFID()).windTunnel().addPointOfInterest( getExecutionId( webDriver ), getPoi() + "(" + getPageName() + "." + getName() + ")", Status.failure );
                        }
                        log.warn( Thread.currentThread().getName() + ": ***** Step " + name + " on page " + pageName + " failed");
                        log.info( Thread.currentThread().getName() + ": ***** Step " + name + " on page " + pageName + " failed", currentError );
                        
                        if ( stepException instanceof BubbledFailureException )
                        	throw stepException;
                        
                        return false;

                    case LOG_IGNORE:
                        log.warn( Thread.currentThread().getName() + ": ***** Step " + name + " on page " + pageName + " failed");
                        log.info( Thread.currentThread().getName() + ": Step " + name + " failed but was marked to log and ignore", currentError );

                    case IGNORE:
                        if ( currentError == null )
                        {
                            if ( stepException == null )
                                stepException = new ScriptConfigurationException( toError() );

                            if ( isTimed() )
                                recordTimings( (DeviceWebDriver)webDriver, executionContext, System.currentTimeMillis(), StepStatus.FAILURE );

                            if ( PageManager.instance( executionContext.getxFID() ).isWindTunnelEnabled() && getPoi() != null && !getPoi().isEmpty() )
                                PerfectoMobile.instance(((DeviceWebDriver)webDriver).getxFID()).windTunnel().addPointOfInterest( getExecutionId( webDriver ), getPoi() + "(" + getPageName() + "." + getName() + ")", Status.failure );
                        }

                        executionContext.setFailedStep( null );
                        returnValue = !subFailure;

                }
            }

            return returnValue;

        }
        catch ( KWSLoopBreak lb )
        {
            throw lb;
        }
        catch ( FlowException lb )
        {
            executionContext.completeStep( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, null, getReportMessage( lb.isSuccess() ? StepStatus.SUCCESS : StepStatus.FAILURE, contextMap, dataMap, ( (DeviceWebDriver) webDriver ).getxFID() ) );
            throw lb;
        }
        finally
        {
            if ( waitTime > 0 )
            {
                try
                {
                    Thread.sleep( waitTime );
                }
                catch ( Exception e )
                {

                }
            }
        }
    }

     
    protected void recordTimings( DeviceWebDriver webDriver, ExecutionContextTest executionContext, long runLength, StepStatus status )
    {
        ExecutionContextStep currentStep = executionContext.getStep();
        
        if ( webDriver.getAut() != null && webDriver.getAut().isWeb() )
        {
            try
            {
            Map<String,Long> performanceData = (Map<String,Long>)webDriver.executeScript( "var perfData = window.performance.timing; return perfData;", HASH_CACHE );
            
            for ( String perfKey : WEB_PERFORMANCE )
            {
                try
                {
                    Long value = performanceData.get( perfKey );
                    if ( value != null )
                        currentStep.addTiming( perfKey, value );
                }
                catch( Exception e )
                {
                    
                }
            }
            }
            catch( Exception e )
            {
                log.warn( "Could not capture WEB performance data " + e.getMessage() );
            }
        }
        
        if ( PageManager.instance( executionContext.getxFID() ).isWindTunnelEnabled() )
            PerfectoMobile.instance(webDriver.getxFID()).windTunnel().addTimerReport( webDriver.getExecutionId(), getPageName() + "." + getName() + "." + getClass().getSimpleName(), (int) runLength, ((status.equals( StepStatus.SUCCESS ) || (status.equals( StepStatus.FAILURE_IGNORED ))) ? Status.success : Status.failure), description, threshold );
        
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setName(java.lang.String)
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setPageName(java.lang.String)
     */
    public void setPageName( String pageName )
    {
        this.pageName = pageName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setActive(boolean)
     */
    public void setActive( boolean active )
    {
        this.active = active;
    }

    /**
     * Gets the parameter list.
     *
     * @return the parameter list
     */
    public List<KeyWordParameter> getParameterList()
    {
        return parameterList;
    }

    public KeyWordParameter getParameter( String parameterName )
    {
        for ( KeyWordParameter p : parameterList )
        {
            if ( parameterName.equalsIgnoreCase( p.getName() ) )
                return p;
        }
        return null;
    }
    
    protected String getParameterAsString( String parameterName, Map<String, Object> contextMap, Map<String, PageData> dataMap, String xFID )
    {
        for ( KeyWordParameter p : parameterList )
        {
            if ( parameterName.equalsIgnoreCase( p.getName() ) )
            {
                return getParameterValue( p, contextMap, dataMap, xFID );
            }
        }
        return null;
    }
    

    /**
     * Gets the token list.
     *
     * @return the token list
     */
    public List<KeyWordToken> getTokenList()
    {
        return tokenList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getStepList()
     */
    public List<KeyWordStep> getStepList()
    {
        return stepList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getLinkId()
     */
    public String getLinkId()
    {
        return linkId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#setLinkId(java.lang.String)
     */
    public void setLinkId( String linkId )
    {
        this.linkId = linkId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#addParameter(com.
     * perfectoMobile.page.keyWord.KeyWordParameter)
     */
    public void addToken( KeyWordToken token )
    {
        if ( log.isDebugEnabled() )
            log.debug( "Adding Token " + token.getValue() );
        tokenList.add( token );
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#addParameter(com.
     * perfectoMobile.page.keyWord.KeyWordParameter)
     */
    public void addParameter( KeyWordParameter param )
    {
        if ( log.isDebugEnabled() )
            log.debug( "Adding Parameter " + param.getValue() );
        parameterList.add( param );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.perfectoMobile.page.keyWord.KeyWordStep#addStep(com.perfectoMobile.
     * page.keyWord.KeyWordStep)
     */
    public void addStep( KeyWordStep step )
    {
        if ( log.isDebugEnabled() )
            log.debug( "Adding Sub Step " + step.getName() );
        stepList.add( step );
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#addAllSteps(com.
     * perfectoMobile.page.keyWord.KeyWordStep[])
     */
    public void addAllSteps( KeyWordStep[] step )
    {
        if ( step != null )
        {
            for ( KeyWordStep s : step )
                addStep( s );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getName()
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#isActive()
     */
    public boolean isActive()
    {
        return active;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getPageName()
     */
    public String getPageName()
    {
        return pageName;
    }

    /**
     * Gets the page data.
     *
     * @param recordIdentifier
     *            the record identifier
     * @param dataMap
     *            the data map
     * @return the page data
     */
    private PageData getPageData( String recordIdentifier, Map<String, PageData> dataMap )
    {
        //
        // First, lets see if this table was cached with the full key
        //
        int dotPosition = recordIdentifier.lastIndexOf( "." );
        String tableName = recordIdentifier.substring( 0, dotPosition );

        PageData pageData = dataMap.get( tableName );
        if ( pageData != null )
            return pageData;

        String[] tableParts = tableName.split( "\\." );

        for ( String currentTable : tableParts )
        {
            if ( pageData != null )
            {
                List<PageData> dataList = pageData.getPageData( currentTable );
                if ( dataList == null || dataList.isEmpty() )
                    log.error( "Could not locate " + currentTable + " in " + recordIdentifier );

                pageData = dataList.get( 0 );
            }
            else
            {
                pageData = dataMap.get( currentTable );
                if ( pageData == null )
                    log.error( "Could not locate " + currentTable + " in " + recordIdentifier );
            }
        }

        return pageData;
    }

    /**
     * Gets the parameter value.
     *
     * @param param
     *            the param
     * @param contextMap
     *            the context map
     * @param dataMap
     *            the data map
     * @return the parameter value
     */
    public String getParameterValue( KeyWordParameter param, Map<String, Object> contextMap, Map<String, PageData> dataMap, String xFID )
    {
        if ( param == null )
            return null;
        Object returnValue;
        switch ( param.getType() )
        {
            case CONTEXT:
                returnValue = contextMap.get( param.getValue() );
                break;

            case STATIC:
                returnValue = param.getValue();
                break;

            case PROPERTY:
                returnValue = System.getProperty( param.getValue(), "" );
                if ( "".equals(returnValue) )
                    returnValue = KeyWordDriver.instance( xFID ).getProperty( param.getValue() );
                
                if ( returnValue == null )
                    returnValue = "";
                break;

            case CONTENT:
                returnValue = ContentManager.instance(xFID).getContentValue( param.getValue() );
                break;

            case DATA:

                //
                // We allow data to be overriden by context
                //
                if ( contextMap.get( param.getValue() ) != null )
                {
                    returnValue = contextMap.get( param.getValue() );
                    break;
                }

                
                int dotPosition = param.getValue().lastIndexOf( "." );
                String tableName = param.getValue().substring( 0, dotPosition );
                String recordName = param.getValue().substring( param.getValue().lastIndexOf( "." ) + 1 );
                PageData pageData = getPageData( param.getValue(), dataMap );
                if ( pageData == null )
                {
                    throw new ScriptConfigurationException( Thread.currentThread().getName() + ": The Page Data record type [" + tableName + "] does not exist for this test - check your dataProvider or dataDriver attribute" );
                }

                returnValue = pageData.getData( recordName );

                if ( returnValue == null )
                    throw new ScriptConfigurationException(
                            Thread.currentThread().getName() + ": The Page Data field [" + recordName + "] does not exist for the page data record type [" + tableName + "] - Reference one of the following fields - " + pageData );
                break;

            case FILE:
                returnValue = param.getValue();

                break;

            default:
                throw new ScriptConfigurationException( Thread.currentThread().getName() + ": Unknown Parameter Type [" + param.getValue() + "]" );
        }
        if ( param.getTokenList() != null )
        {
            for ( KeyWordToken token : param.getTokenList() )
            {
                if ( log.isDebugEnabled() )
                    log.debug( "Applying token " + token.getName() );

                returnValue = ( returnValue + "").replace( "{" + token.getName() + "}", getTokenValue( token, contextMap, dataMap, xFID ) );
            }
        }
        
        try
        {
          boolean valueReplaced = false;
          do
          {
            valueReplaced = false;
            Matcher m = TOKEN_REGEX.matcher( returnValue + "" );
            while( m.find() )
            {
              String propertyValue = KeyWordDriver.instance( xFID ).getProperty( m.group(1) );
              if ( propertyValue != null )
              {
                returnValue = ( returnValue + "" ).replace( "{" + m.group( 1 ) + "}", propertyValue );
                valueReplaced = true;
                break;
              }
            }
          }
          while( valueReplaced );
        }
        catch( Exception e )
        {
          
        }

        if ( returnValue == null )
            return "";
        else
        {
            //
            // Apply value transformation 
            //
            return ValueTransformationFactory.instance().transformValue( returnValue + "" );
        }
    }

    /**
     * Gets the parameter value.
     *
     * @param token
     *            the token
     * @param contextMap
     *            the context map
     * @param dataMap
     *            the data map
     * @return the parameter value
     */
    public String getTokenValue( KeyWordToken token, Map<String, Object> contextMap, Map<String, PageData> dataMap, String xFID )
    {
        String returnValue = null;
        switch ( token.getType() )
        {
            case CONTEXT:
                returnValue = contextMap.get( token.getValue() ) + "";
                break;

            case STATIC:
                returnValue = token.getValue();
                break;

            case PROPERTY:
                returnValue = System.getProperty( token.getValue(), "" );
                if ( "".equals(returnValue) )
                    returnValue = KeyWordDriver.instance( xFID ).getProperty( token.getValue() );
                
                if ( returnValue == null )
                    returnValue = "";
                
                break;

            case CONTENT:
                returnValue = ContentManager.instance(xFID).getContentValue( token.getValue() + "" );
                break;
                
            case DATA:
                //
                // We allow data to be overriden by context
                //
                if ( contextMap.get( token.getValue() ) != null )
                    return contextMap.get( token.getValue() ) + "";
                
                int dotPosition = token.getValue().lastIndexOf( "." );
                String tableName = token.getValue().substring( 0, dotPosition );
                String recordName = token.getValue().substring( token.getValue().lastIndexOf( "." ) + 1 );
                PageData pageData = getPageData( token.getValue(), dataMap );
                if ( pageData == null )
                {
                    throw new ScriptConfigurationException( Thread.currentThread().getName() + ": The Page Data record type [" + tableName + "] does not exist for this test - chexk your dataProvider or dataDriver attribute" );
                }

                Object rV = pageData.getData( recordName );

                if ( rV == null )
                    throw new ScriptConfigurationException(
                            Thread.currentThread().getName() + ": The Page Data field [" + recordName + "] does not exist for the page data record type [" + tableName + "] - Reference one of the following fields - " + pageData );

                returnValue = rV + "";
                break;

            default:
                throw new ScriptConfigurationException( Thread.currentThread().getName() + ": Unknown Token Type [" + token.getValue() + "]" );
        }
        
        if ( returnValue == null )
            return "";
        else
        {
            //
            // Apply value transformation 
            //
            return ValueTransformationFactory.instance().transformValue( returnValue + "" );
        }
    }

    /**
     * Gets the parameters.
     *
     * @param contextMap
     *            the context map
     * @param dataMap
     *            the data map
     * @return the parameters
     */
    protected Object[] getParameters( Map<String, Object> contextMap, Map<String, PageData> dataMap, String xFID )
    {
        Object[] parameterArray = new Object[parameterList.size()];

        for ( int i = 0; i < parameterList.size(); i++ )
        {
            parameterArray[i] = getParameterValue( parameterList.get( i ), contextMap, dataMap, xFID );
            
            if ( parameterArray[ i ] instanceof String )
            {
            		Matcher m = DATA_PATTERN.matcher( (String) parameterArray[ i ] );
            		if ( m.matches() )
            		{
            			//
            			// A page data object was specified so insert it here
            			//
            			parameterArray[ i ] = dataMap.get( m.group( 1 ) );
            		}
            }
            
            
        }

        return parameterArray;
    }

    /**
     * Find method.
     *
     * @param rootClass
     *            the root class
     * @param methodName
     *            the method name
     * @param args
     *            the args
     * @return the method
     */
    protected Method findMethod( Class rootClass, String methodName, Object[] args )
    {
        Method[] methodArray = rootClass.getMethods();

        for ( Method currentMethod : methodArray )
        {
            if ( isCorrectMethod( currentMethod, methodName, args ) )
            {
                if ( log.isDebugEnabled() )
                    log.debug( "Found [" + methodName + "] on " + rootClass.getName() );

                if ( log.isDebugEnabled() && args != null )
                {
                    StringBuilder pBuilder = new StringBuilder();

                    pBuilder.append( args.length ).append( " paramters supplied as: \r\n" );

                    for ( Object arg : args )
                    {
                        pBuilder.append( "\t" );
                        if ( arg == null )
                            pBuilder.append( "NULL" );
                        else
                            pBuilder.append( "[" + arg.toString() + "] of type " + arg.getClass().getName() );
                        pBuilder.append( "\r\n" );

                    }
                    log.debug( pBuilder.toString() );
                }

                return currentMethod;
            }
        }

        if ( log.isWarnEnabled() )
        {
            StringBuilder pBuilder = new StringBuilder();
            pBuilder.append( "Could not locate " ).append( methodName ).append( " with " );
            pBuilder.append( args.length ).append( " paramters supplied as: \r\n" );

            for ( Object arg : args )
            {
                pBuilder.append( "\t" );
                if ( arg == null )
                    pBuilder.append( "NULL" );
                else
                    pBuilder.append( "[" + arg.toString() + "] of type " + arg.getClass().getName() );
                pBuilder.append( "\r\n" );

            }
            log.warn( pBuilder.toString() );
        }
        return null;

    }

    /**
     * Checks if is correct method.
     *
     * @param compareMethod
     *            the compare method
     * @param methodName
     *            the method name
     * @param parameterArray
     *            the parameter array
     * @return true, if is correct method
     */
    protected boolean isCorrectMethod( Method compareMethod, String methodName, Object[] parameterArray )
    {
        if ( !methodName.equals( compareMethod.getName() ) )
            return false;

        if ( (parameterArray == null || parameterArray.length == 0) && (compareMethod.getParameterTypes() == null || compareMethod.getParameterTypes().length == 0) )
            return true;

        if ( parameterArray == null || compareMethod.getParameterTypes() == null )
            return false;

        Class[] parameterTypes = compareMethod.getParameterTypes();

        if ( parameterTypes.length != parameterArray.length )
        {
            if ( log.isDebugEnabled() )
                log.debug( Thread.currentThread().getName() + ": Paramter Count Mismatch " + parameterTypes.length + " - " + parameterArray.length );
            return false;
        }

        for ( int i = 0; i < parameterArray.length; i++ )
        {
            if ( log.isDebugEnabled() )
                log.debug( parameterTypes[i] + " - " + parameterArray[i] );
            if ( !isInstance( parameterTypes[i], parameterArray[i] ) )
                return false;
        }

        return true;
    }

    /**
     * Checks if is instance.
     *
     * @param classType
     *            the class type
     * @param value
     *            the value
     * @return true, if is instance
     */
    protected boolean isInstance( Class classType, Object value )
    {
        try
        {
            if ( classType.isPrimitive() )
            {
                if ( value == null )
                {
                    if ( log.isDebugEnabled() )
                        log.debug( "Primative value null" );
                    return false;
                }
                else
                {
                    Field typeField = value.getClass().getField( TYPE );
                    return classType.isAssignableFrom( (Class) typeField.get( value ) );
                }
            }
            else
            {
                return (value == null || classType.isInstance( value ));
            }
        }
        catch ( Exception e )
        {
            log.error( "Error getting instance", e );
            return false;
        }
    }

    /**
     * Gets the execution id.
     *
     * @param webDriver
     *            the web driver
     * @return the execution id
     */
    public String getExecutionId( WebDriver webDriver )
    {
        return PageManager.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).getExecutionId( webDriver );
    }

    /**
     * Gets the execution id.
     *
     * @param webDriver
     *            the web driver
     * @return the execution id
     */
    public String getDeviceOs( WebDriver webDriver )
    {
        return PageManager.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).getDeviceOs( webDriver );
    }

    /**
     * Gets the device name.
     *
     * @param webDriver
     *            the web driver
     * @return the device name
     */
    public String getDeviceName( WebDriver webDriver )
    {
        return PageManager.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).getDeviceName( webDriver );
    }

    /**
     * Validate data.
     *
     * @param dataValue
     *            the data value
     * @return true, if successful
     */
    protected boolean validateData( String dataValue )
    {
        if ( validationType == null )
            return true;

        switch ( validationType )
        {
            case EMPTY:
                return dataValue == null || dataValue.isEmpty();

            case REGEX:
                if ( dataValue == null )
                {
                    log.warn( "REGEX validation specified with a blank value" );
                    return true;
                }
                else
                {
                    log.debug( Thread.currentThread().getName() + ": Attempting to analyze [" + dataValue + "] using the Regular Expression [" + validation + "]" );
                    if ( !dataValue.matches( validation ) )
                    {
                        log.error( Thread.currentThread().getName() + ": Validation failed for [" + dataValue + "] using the Regular Expression [" + validation + "]" );
                        return false;
                    }
                    return true;
                }

            case NOT_EMPTY:
                return dataValue != null && !dataValue.isEmpty();
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getValidation()
     */
    public String getValidation()
    {
        return validation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setValidation(java.lang.
     * String)
     */
    public void setValidation( String validation )
    {
        this.validation = validation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getValidationType()
     */
    public ValidationType getValidationType()
    {
        return validationType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setValidationType(com.
     * perfectoMobile.page.keyWord.KeyWordStep.ValidationType)
     */
    public void setValidationType( ValidationType validationType )
    {
        this.validationType = validationType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#getWait()
     */
    public long getWait()
    {
        return waitTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.perfectoMobile.page.keyWord.KeyWordStep#setWait(long)
     */
    public void setWait( long waitAfter )
    {
        this.waitTime = waitAfter;
    }

    public String dumpState(WebDriver webDriver, Map<String, Object> contextMap, Map<String, PageData> dataMap, ExecutionContextTest executionContext )
    {
        return dumpState(webDriver, contextMap, dataMap, executionContext, null);
    }
    
    public String dumpState(WebDriver webDriver, Map<String, Object> contextMap, Map<String, PageData> dataMap, ExecutionContextTest executionContext, String useName )
    {
        String checkPointName = null;
        int historicalCount = -1;
        int deviationPercentage = -1;

        File checkPointFolder = null;

        if ( useName != null )
            checkPointName = useName;
        else
            checkPointName = getParameterValue( getParameter( "checkPointName" ), contextMap, dataMap, executionContext.getxFID() );
        
        if ( checkPointName != null )
        {
            checkPointFolder = new File( DataManager.instance(( (DeviceWebDriver) webDriver ).getxFID()).getReportFolder(), "historicalComparison" );
            checkPointFolder = new File( checkPointFolder, ((DeviceWebDriver) webDriver).getDevice().getKey() );

            String hCount = getParameterValue( getParameter( "historicalCount" ), contextMap, dataMap, executionContext.getxFID() );
            if ( hCount != null )
            {
                historicalCount = Integer.parseInt( hCount );

                String dP = getParameterValue( getParameter( "deviationPercentage" ), contextMap, dataMap, executionContext.getxFID() );
                if ( dP != null )
                    deviationPercentage = Integer.parseInt( dP  );
            }
        }

        File rootFolder = ExecutionContext.instance(( (DeviceWebDriver) webDriver ).getxFID()).getReportFolder(( (DeviceWebDriver) webDriver ).getxFID());
        File useFolder = new File( rootFolder, "artifacts" );
        useFolder.mkdirs();

        File screenFile = null;
        File domFile = null;
        File xpathFile = null;

        if ( webDriver instanceof TakesScreenshot )
        {
            OutputStream os = null;
            try
            {
                byte[] screenShot = ((TakesScreenshot) webDriver).getScreenshotAs( OutputType.BYTES );
                if ( checkPointName != null )
                    screenFile = new File( useFolder, "grid-" + checkPointName.replace( "-", "_" ) + "-" + ((DeviceWebDriver) webDriver).getDevice().getKey() + ".png" );
                else
                    screenFile = File.createTempFile( "state", ".png", useFolder );

                if ( contextMap != null )
                {
                    addContext( "_SCREENSHOT_ABS", screenFile.getAbsolutePath(), contextMap, executionContext );
                    addContext( "_SCREENSHOT", "../../artifacts/" + screenFile.getName(), contextMap, executionContext );
                }
                
                executionContext.getStep().addExecutionParameter( "SCREENSHOT_ABS", screenFile.getPath() );
                executionContext.getStep().addExecutionParameter( "SCREENSHOT", "../../artifacts/" + screenFile.getName() );
                screenFile.getParentFile().mkdirs();
                os = new BufferedOutputStream( new FileOutputStream( screenFile ) );
                os.write( screenShot );
                os.flush();
                os.close();

                if ( checkPointFolder != null )
                {
                    //
                    // Dump state as historical
                    //
                    checkPointFolder.mkdirs();

                    if ( historicalCount >= 0 )
                    {
                        List<File> historicalFiles = Arrays.asList( checkPointFolder.listFiles( new CheckPointFiles( checkPointName + "-" ) ) );
                        Collections.sort( historicalFiles );
                        Collections.reverse( historicalFiles );

                        if ( historicalFiles.size() >= historicalCount )
                            historicalFiles.get( 0 ).delete();

                        for ( File file : historicalFiles )
                        {
                            String[] fileParts = file.getName().split( "\\." );
                            int fileNumber = Integer.parseInt( fileParts[0].substring( checkPointName.length() + 1 ) );

                            file.renameTo( new File( file.getParentFile(), checkPointName + "-" + numberFormat.format( fileNumber + 1 ) + ".png" ) );
                        }

                        os = new BufferedOutputStream( new FileOutputStream( new File( checkPointFolder, checkPointName + "-0000.png" ) ) );
                    }
                    else
                        os = new BufferedOutputStream( new FileOutputStream( new File( checkPointFolder, checkPointName + ".png" ) ) );

                    os.write( screenShot );
                    os.flush();
                    os.close();

                    if ( deviationPercentage >= 0 )
                    {
                        //
                        // Compare with the last image
                        //
                        File newFile = new File( checkPointFolder, checkPointName + "-0000.png" );
                        File previousFile = new File( checkPointFolder, checkPointName + "-0001.png" );
                        
                        if ( newFile.exists() && previousFile.exists() )
                        {
                            double computedDeviation = (ImageUtility.compareImages( ImageIO.read( newFile ), ImageIO.read( previousFile ) ) * 100.0);

                            
                            if ( computedDeviation > deviationPercentage )
                                throw new ScriptException( "Historical image comparison failed.  Expected a maximum difference of [" + deviationPercentage + "] but found [" + computedDeviation + "]" );
                        }
                    }
                }
            }
            catch ( ScriptException xe )
            {
                throw xe;
            }
            catch ( Exception e )
            {
                log.debug( "Error taking screenshot", e );
                throw new ScriptException( e.getMessage() );
            }
            finally
            {
                if ( os != null )
                    try
                    {
                        os.close();
                    }
                    catch ( Exception e )
                    {
                    }
            }
        }

        String pageSource = null;
        
        FileOutputStream outputStream = null;
        try
        {
            File xmlFile = File.createTempFile( "dom-", ".xml", useFolder );
            domFile = new File( xmlFile.getParentFile(), xmlFile.getName().replace( ".xml", ".html" ) );
            xpathFile = new File( domFile.getParentFile(), domFile.getName().replace( ".html", "-xpath.html" ) );

            if ( contextMap != null )
            {
                addContext( "_DOM_XML_ABS", xmlFile.getAbsolutePath(), contextMap, executionContext );
                addContext( "_DOM_XML", "../../artifacts/" + xmlFile.getName(), contextMap, executionContext );
                addContext( "_DOM_HTML_ABS", domFile.getAbsolutePath(), contextMap, executionContext );
                addContext( "_DOM_HTML", "../../artifacts/" + domFile.getName(), contextMap, executionContext );
            }

            executionContext.getStep().addExecutionParameter( "XML_ABS", xmlFile.getPath() );
            executionContext.getStep().addExecutionParameter( "HTML_ABS", domFile.getPath() );
            executionContext.getStep().addExecutionParameter( "XPATH_ABS", xpathFile.getPath() );
            executionContext.getStep().addExecutionParameter( "XML", "../../artifacts/" + xmlFile.getName() );
            executionContext.getStep().addExecutionParameter( "HTML", "../../artifacts/" + domFile.getName() );
            executionContext.getStep().addExecutionParameter( "XPATH", "../../artifacts/" + xpathFile.getName() );

            pageSource = webDriver.getPageSource();
            outputStream = new FileOutputStream( xmlFile );
            
            outputStream.write( XMLEscape.toXML( pageSource ).getBytes() );

            outputStream.flush();
            outputStream.close();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append( "<html><head><link href=\"http://www.xframium.org/output/assets/css/prism.css\" rel=\"stylesheet\"><script src=\"http://www.xframium.org/output/assets/js/prism.js\"></script>" );
            stringBuilder.append( "</script></head><body><pre class\"line-numbers\"><code class=\"language-markup\">" + pageSource.replace( "<", "&lt;" ).replace( ">", "&gt;" ).replace( "\t", "  " ) + "</code></pre></body></html>" );

            outputStream = new FileOutputStream( domFile );
            outputStream.write( stringBuilder.toString().getBytes() );
            outputStream.flush();
            outputStream.close();
            
            InputStream xStream = getClass().getResourceAsStream( "/org/xframium/resource/xpath.html" );
            StringBuilder xBuilder = new StringBuilder();
            byte[] buffer = new byte[ 512 ];
            int bytesRead = 0;
            while ( (bytesRead = xStream.read( buffer ) ) != -1 )
            {
                xBuilder.append( new String( buffer, 0, bytesRead) );
            }
            
            String xPathData = xBuilder.toString();
            String xmlData = removeScript( XMLEscape.toXML( pageSource ).replace( "\r", " " ).replace( "\n", " " ).replace( "'", "\\'" ) );
            
            outputStream = new FileOutputStream( xpathFile );
            outputStream.write( xPathData.replace( "{xmlData}", xmlData ).replace( "{sourceFile}", domFile.getName() ).getBytes() );
            outputStream.flush();
            outputStream.close();
        }
        catch ( Exception e )
        {
            log.warn( "Could not write to output file", e );
            throw new ScriptException( "Error capturing device source" );
        }
        finally
        {
            if ( outputStream != null )
                try
                {
                    outputStream.close();
                }
                catch ( Exception e )
                {
                }
        }
        
        return pageSource;
    }

    
    private static String removeScript( String o )
    {
        String newString = o;
        
        int scriptLocation = 0;
        
        while ( ( scriptLocation = newString.indexOf( "<script" ) ) != -1 )
        {
            int endLocation = newString.indexOf( "/script>", scriptLocation );
            if ( endLocation != -1 )
            {
                String begin = newString.substring( 0, scriptLocation );
                String end = newString.substring( endLocation + 8 );
                newString = begin + end;
            }
            else
                break;
                
        }
        
        return newString;
    }
    
    private class CheckPointFiles implements FilenameFilter
    {
        private String checkPointName;

        public CheckPointFiles( String checkPointName )
        {
            this.checkPointName = checkPointName;
        }

        @Override
        public boolean accept( File dir, String name )
        {
            return (name.startsWith( checkPointName ));
        }
    }
}
