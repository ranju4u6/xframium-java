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
package org.xframium.page.keyWord;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.xframium.container.SuiteContainer;
import org.xframium.device.DeviceManager;
import org.xframium.device.factory.DeviceWebDriver;
import org.xframium.device.ng.TestName;
import org.xframium.exception.DataConfigurationException;
import org.xframium.exception.FilteredException;
import org.xframium.exception.FlowException;
import org.xframium.exception.ScriptConfigurationException;
import org.xframium.exception.ScriptException;
import org.xframium.exception.TestConfigurationException;
import org.xframium.exception.XFramiumException;
import org.xframium.page.Page;
import org.xframium.page.PageManager;
import org.xframium.page.StepStatus;
import org.xframium.page.data.PageData;
import org.xframium.page.data.PageDataManager;
import org.xframium.page.keyWord.step.SyntheticStep;
import org.xframium.page.listener.KeyWordListener;
import org.xframium.reporting.ExecutionContextTest;
import org.xframium.reporting.ExecutionContextTest.TestStatus;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyWordDriver.
 */
public class KeyWordDriver
{

    private List<String> testList = new ArrayList<String>( 20 );

    /** The test map. */
    private Map<String, KeyWordTest> testMap = new HashMap<String, KeyWordTest>( 10 );

    /** The inactive test map. */
    private Map<String, KeyWordTest> inactiveTestMap = new HashMap<String, KeyWordTest>( 10 );

    /** The function map. */
    private Map<String, KeyWordTest> functionMap = new HashMap<String, KeyWordTest>( 10 );

    /** The page map. */
    private Map<String, Class> pageMap = new HashMap<String, Class>( 10 );

    /** The tag map. */
    private Map<String, List<KeyWordTest>> tagMap = new HashMap<String, List<KeyWordTest>>( 10 );

    /** The singleton. */
    private static Map<String,KeyWordDriver> singleton = new HashMap<String,KeyWordDriver>(5);

    /** The configuration properties **/
    private Properties configProperties = new Properties();

    private List<KeyWordListener> stepListenerList = new ArrayList<KeyWordListener>( 10 );

    private List<String> tagList = new ArrayList<String>( 4 );
    
    private Map<String,GherkinContainer> methodMap = new HashMap<String,GherkinContainer>( 25 );
    
    public enum TRACE
    {
        OFF,
        ON,
        DISABLED;
    }
    
    private TRACE trace = TRACE.OFF;
    
    public Map<String,GherkinContainer> getMethodMap()
    {
    		return methodMap;
    }
    
    public void addTag( String tagName )
    {
        tagList.add( tagName );
    }
    
    public void removeTag( String tagName )
    {
        tagList.remove( tagName );
    }
    
    public String[] getTags()
    {
        return tagList.toArray( new String[ 0 ] );
    }
    
    public TRACE getTrace()
    {
        return trace;
    }

    public void setTrace(TRACE trace)
    {
        this.trace = trace;
    }

    /**
     * Instance.
     *
     * @return the key word driver
     */
    public static KeyWordDriver instance( String xFID )
    {
        if ( singleton.containsKey( xFID ) )
            return singleton.get( xFID );
        else
        {
            singleton.put( xFID, new KeyWordDriver( xFID ) );
            return singleton.get( xFID );
        }
    }
    
    private String xFID;
    public KeyWordDriver( String xFID )
    {
        this.xFID = xFID;
    }

    public void clear()
    {
        testList.clear();
        testMap.clear();
        inactiveTestMap.clear();
        functionMap.clear();
        tagMap.clear();
        pageMap.clear();
        configProperties.clear();
    }

    /**
     * Load tests.
     *
     * @param keyWordProvider
     *            the key word provider
     */
    public void loadTests( SuiteContainer sC )
    {
        
        for ( KeyWordTest t : sC.getActiveTestList() )
        {
            addTest( t );
        }
        
        for ( KeyWordTest t : sC.getInactiveTestList() )
        {
            addTest( t );
        }
        
        for ( KeyWordTest t : sC.getFunctionList() )
        {
            addFunction( t );
        }
        
        
    }

    /** The log. */
    private Log log = LogFactory.getLog( KeyWordDriver.class );

    /**
     * Adds the page.
     *
     * @param pageName
     *            the page name
     * @param pageClass
     *            the page class
     */
    public void addPage( String siteName, String pageName, Class pageClass )
    {
        String useName = null;
        if ( siteName != null )
            useName = siteName + "." + pageName;
        else
            useName = PageManager.instance(xFID).getSiteName() + "." + pageName;
        if ( log.isInfoEnabled() )
            log.info( "Mapping Page [" + useName + "] to [" + pageClass.getName() + "]" );
        pageMap.put( useName, pageClass );
    }

    public void addStepListener( KeyWordListener stepListener )
    {
        for ( KeyWordListener s : stepListenerList )
        {
            if ( s.equals( stepListener ) )
                return;
        }

        stepListenerList.add( stepListener );
    }
    
    public void removeStepListener( Class listenerClass )
    {
      List<KeyWordListener> removeList = new ArrayList<KeyWordListener>( 10 );
        for ( KeyWordListener l : stepListenerList )
        {
          if ( l.getClass().isAssignableFrom( listenerClass ) )
            removeList.add( l );
        }
        
        for ( KeyWordListener l : removeList )
          stepListenerList.remove( l );
    }

    public void removeStepListener( KeyWordListener stepListener )
    {
        stepListenerList.remove( stepListener );
    }

    public boolean notifyBeforeStep( WebDriver webDriver, KeyWordStep currentStep, Page pageObject, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, SuiteContainer sC, ExecutionContextTest eC  )
    {
        try
        {
            for ( KeyWordListener k : stepListenerList )
            {
                if ( !k.beforeStep( webDriver, currentStep, pageObject, contextMap, dataMap, pageMap, sC, eC ) )
                    return false;
            }

            return true;
        }
        catch ( Exception e )
        {
            log.warn( "Before Step notification failed", e );
            return true;
        }
    }

    public void notifyAfterStep( WebDriver webDriver, KeyWordStep currentStep, Page pageObject, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, StepStatus stepStatus, SuiteContainer sC, ExecutionContextTest eC  )
    {
        try
        {
            for ( KeyWordListener k : stepListenerList )
            {
                k.afterStep( webDriver, currentStep, pageObject, contextMap, dataMap, pageMap, stepStatus, sC, eC );
            }
        }
        catch ( Exception e )
        {
            log.warn( "After Step notification failed", e );

        }

    }

    public boolean notifyBeforeTest( WebDriver webDriver, KeyWordTest keyWordTest, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, SuiteContainer sC, ExecutionContextTest eC  )
    {
        try
        {
            for ( KeyWordListener k : stepListenerList )
            {
                if ( !k.beforeTest( webDriver, keyWordTest, contextMap, dataMap, pageMap, sC, eC ) )
                    return false;
            }

            return true;
        }
        catch ( Exception e )
        {
            log.warn( "Before Test notification failed", e );
            return true;
        }
    }

    public void notifyAfterTest( WebDriver webDriver, KeyWordTest keyWordTest, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, boolean stepPass, SuiteContainer sC, ExecutionContextTest eC  )
    {
        try
        {
            for ( KeyWordListener k : stepListenerList )
            {
                k.afterTest( webDriver, keyWordTest, contextMap, dataMap, pageMap, stepPass, sC, eC );
            }
        }
        catch ( Exception e )
        {
            log.warn( "After Test notifications failed", e );
        }
    }
    
    public void notifyAfterArtifacts( WebDriver webDriver, KeyWordTest keyWordTest, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, boolean stepPass, SuiteContainer sC, ExecutionContextTest eC  )
    {
        try
        {
            for ( KeyWordListener k : stepListenerList )
            {
                k.afterArtifacts( webDriver, keyWordTest, contextMap, dataMap, pageMap, stepPass, sC, eC );
            }
        }
        catch ( Exception e )
        {
            log.warn( "After artifact notifications failed", e );
        }
    }

    /**
     * Adds the test.
     *
     * @param test
     *            the test
     */
    public void addTest( KeyWordTest test )
    {
        if ( test.isActive() )
        {
            if ( log.isInfoEnabled() )
                log.info( "Adding test [" + test.getName() + "]" );
            testList.add( test.getName() );
            testMap.put( test.getName(), test );

            //
            // Add the Tagged tests
            //
            for ( String tag : test.getTags() )
            {
                List<KeyWordTest> tagList = tagMap.get( tag.toLowerCase() );
                if ( tagList == null )
                {
                    tagList = new ArrayList<KeyWordTest>( 10 );
                    tagMap.put( tag.toLowerCase(), tagList );
                }

                tagList.add( test );
            }
        }
        else
            inactiveTestMap.put( test.getName(), test );
    }

    /**
     * Adds the function.
     *
     * @param test
     *            the test
     */
    public void addFunction( KeyWordTest test )
    {
        if ( test.isActive() )
        {
            if ( log.isInfoEnabled() )
                log.info( "Adding function [" + test.getName() + "]" );
            functionMap.put( test.getName(), test );
        }
    }

    /**
     * Gets the page.
     *
     * @param pageName
     *            the page name
     * @return the page
     */
    public Class getPage( String siteName, String pageName )
    {
        String useName = siteName + "." + pageName;
        
        Class pageClass = pageMap.get( useName );
        if ( pageClass == null )
        {
            addPage( siteName, pageName, KeyWordPage.class );
            return KeyWordPage.class;
        }
        
        return pageClass;
    }

    /**
     * Gets the page count
     *
     * @return count
     */
    public int getPageCount()
    {
        return pageMap.size();
    }

    /**
     * Execution function.
     *
     * @param testName
     *            the test name
     * @param webDriver
     *            the web driver
     * @param dataMap
     *            the data map
     * @param pageMap
     *            the page map
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    public boolean executionFunction( String testName, WebDriver webDriver, Map<String, PageData> dataMap, Map<String, Page> pageMap, Map<String,Object> contextMap, SuiteContainer sC, ExecutionContextTest executionContext ) throws Exception
    {
        if ( log.isDebugEnabled() )
            log.debug( "Attempting to locate function/test [" + testName + "]" );

        KeyWordTest test = functionMap.get( testName );
        if ( test == null )
        {
            test = testMap.get( testName );

            if ( test == null )
            {
                test = inactiveTestMap.get( testName );

                if ( test == null )
                    throw new ScriptConfigurationException( "The function [" + testName + "] does not exist" );
            }
        }
        
        test.setTrace( getTrace() );

        if ( test.getDataProviders() != null )
        {
            if ( log.isInfoEnabled() )
                log.info( "Data Provider set as " + test.getDataProvidersAsString() );

            for ( String dataProvider : test.getDataProviders() )
            {
                String dpMe = dataProvider;
                if ( !dataMap.containsKey( dataProvider ) )
                {
                    //
                    // On a function call we only add the page data if it did
                    // not already exist
                    //

                    PageData pageData = null;
                    if ( dataProvider.contains( "." ) )
                    {
                        dpMe = dataProvider.substring( 0, dataProvider.indexOf( "." ) );
                        String recordName = dataProvider.substring( dataProvider.indexOf( "." ) + 1 );
                        pageData = PageDataManager.instance( executionContext.getxFID() ).getPageData( dpMe, recordName );
                    }
                    else if ( dataProvider.contains( "=" ) )
                    {
                        String[] aliasMap = dataProvider.split( "=" );

                        String realName = aliasMap[ 0 ];
                        String alias = aliasMap[ 1 ];
                        
                        pageData = PageDataManager.instance( executionContext.getxFID() ).getPageData( realName );
                        
                        if ( log.isInfoEnabled() )
                            log.info( "Adding Alias " + alias + " for " + realName );
                        dataMap.put( alias, pageData );
                    }
                    else
                    {
                        pageData = PageDataManager.instance( executionContext.getxFID() ).getPageData( dataProvider );
                    }

                    if ( pageData == null )
                        throw new ScriptConfigurationException( "Invalid page data value specified.  Ensure that [" + dataProvider + "] exists in your page data definition" );

                    if ( log.isInfoEnabled() )
                        log.info( "Adding " + dataProvider + " as " + pageData );
                    dataMap.put( dpMe, pageData );
                }
            }
        }

        return test.executeTest( webDriver, contextMap, dataMap, pageMap, sC, executionContext );
    }


    /**
     * Gets the test.
     *
     * @param testName
     *            the test name
     * @return the test
     */
    public KeyWordTest getTest( String testName )
    {
        KeyWordTest test = testMap.get( testName );

        if ( test == null )
            test = inactiveTestMap.get( testName );

        if ( test == null )
            test = functionMap.get( testName );

        return test;
    }

    /**
     * Gets the tagged tests.
     *
     * @param tagNames
     *            the tag names
     * @return the tagged tests
     */
    public Collection<KeyWordTest> getTaggedTests( String[] tagNames )
    {
        Map<String, KeyWordTest> testMap = new HashMap<String, KeyWordTest>( 10 );

        for ( String tagName : tagNames )
        {
            if ( log.isInfoEnabled() )
                log.info( "Adding Tests by TAG [" + tagName.toLowerCase() + "]" );
            for ( KeyWordTest t : tagMap.get( tagName.toLowerCase() ) )
            {
                if ( log.isDebugEnabled() )
                    log.debug( "Adding Test [" + t.getName() + "]" );
                testMap.put( t.getName(), t );
            }
        }

        return testMap.values();
    }

    /**
     * Gets the tagged tests.
     *
     * @param tagNames
     *            the tag names
     * @return the tagged tests
     */
    public Collection<KeyWordTest> getNamedTests( String[] testNames )
    {
        Map<String, KeyWordTest> testMap = new HashMap<String, KeyWordTest>( 10 );

        for ( String tagName : testNames )
        {
            if ( log.isInfoEnabled() )
                log.info( "Adding Tests by NAME [" + tagName.toLowerCase() + "]" );

            for ( KeyWordTest t : this.testMap.values() )
            {
                if ( tagName.contains( "*" ) )
                {
                    if ( tagName.startsWith( "*" ) )
                    {
                        if ( t.getName().toLowerCase().endsWith( tagName.replace( "*", "" ).trim() ) )
                            testMap.put( t.getName(), t );
                    }
                    else if ( tagName.endsWith( "*" ) )
                    {
                        if ( t.getName().toLowerCase().startsWith( tagName.replace( "*", "" ).trim() ) )
                            testMap.put( t.getName(), t );
                    }
                }
                else
                {
                    if ( tagName.toLowerCase().equals( t.getName().toLowerCase() ) )
                        testMap.put( t.getName(), t );
                }
            }
            
            for ( KeyWordTest t : this.inactiveTestMap.values() )
            {
                if ( tagName.contains( "*" ) )
                {
                    if ( tagName.startsWith( "*" ) )
                    {
                        if ( t.getName().toLowerCase().endsWith( tagName.replace( "*", "" ).trim() ) )
                            testMap.put( t.getName(), t );
                    }
                    else if ( tagName.endsWith( "*" ) )
                    {
                        if ( t.getName().toLowerCase().startsWith( tagName.replace( "*", "" ).trim() ) )
                            testMap.put( t.getName(), t );
                    }
                }
                else
                {
                    if ( tagName.toLowerCase().equals( t.getName().toLowerCase() ) )
                        testMap.put( t.getName(), t );
                }

            }
        }
        
        
        

        return testMap.values();
    }

    /**
     * Execute test.
     *
     * @param testName
     *            the test name
     * @param webDriver
     *            the web driver
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    public ExecutionContextTest executeTest( TestName testName, DeviceWebDriver webDriver, SuiteContainer sC ) throws Exception
    {
        boolean testStarted = false;
        boolean returnValue = true;
        PageManager.instance(xFID).getPageCache().clear();

        ExecutionContextTest executionContext = webDriver.getExecutionContext();
        Map<String, PageData> dataMap = new HashMap<String, PageData>( 10 );
        Map<String, Page> pageMap = new HashMap<String, Page>( 10 );
        Map<String, Object> contextMap = new HashMap<String, Object>( 10 );
        KeyWordTest test = testMap.get( testName.getRawName() );
        
        if ( test == null )
            test = inactiveTestMap.get( testName.getRawName() );
        
        logConsole( "Executing [" + testName + "]" );

        try
        {
            if ( test == null )
                throw new TestConfigurationException( testName.getTestName() );
            
            if ( !getTrace().equals( TRACE.OFF ) )
                test.setTrace( getTrace() );
            
            executionContext.setTest( test );
            executionContext.setDevice( webDriver.getPopulatedDevice() );
            
            //
            // Set context variables for populated device
            //
            contextMap.put( "_device.manufacturer", webDriver.getPopulatedDevice().getManufacturer() );
            contextMap.put( "_device.model", webDriver.getPopulatedDevice().getModel() );
            contextMap.put( "_device.os", webDriver.getPopulatedDevice().getOs() );
            contextMap.put( "_device.osVersion", webDriver.getPopulatedDevice().getOsVersion() );
            contextMap.put( "_device.browserName", webDriver.getPopulatedDevice().getBrowserName() );
            contextMap.put( "_device.browserVersion", webDriver.getPopulatedDevice().getBrowserVersion() );
            contextMap.put( "_device.name", webDriver.getPopulatedDevice().getDeviceName() );
            contextMap.put( "_device.phoneNumber", webDriver.getPopulatedDevice().getPhoneNumber() );
            contextMap.put( "_device.resolution", webDriver.getPopulatedDevice().getResolution() );
            contextMap.put( "_device.environment", webDriver.getPopulatedDevice().getEnvironment() );
            contextMap.put( "_device.cloud", webDriver.getPopulatedDevice().getCloud() );
            
            executionContext.setCloud( webDriver.getCloud() );
            
            executionContext.setDataMap( dataMap );
            executionContext.setPageMap( pageMap );
            executionContext.setContextMap( contextMap );
            executionContext.setSessionId( webDriver.getExecutionId() );
            
        
            if ( log.isInfoEnabled() )
                log.info( Thread.currentThread().getName() + ": Configuring Data Providers" );
            if ( test.getDataProviders() != null )
            {
                if ( log.isInfoEnabled() )
                    log.info( "Data Provider set as " + test.getDataProvidersAsString() );

                for ( String dataProvider : test.getDataProviders() )
                {
                    String dpMe = dataProvider;
                    PageData pageData = null;
                    if ( dataProvider.contains( "." ) )
                    {
                        String[] typeId = dataProvider.split( "\\." );
                        if ( typeId.length > 1  )
                        {
                            dpMe = typeId[0];
                            String recordName = dataProvider.substring( dataProvider.indexOf( "." ) + 1 );
                            pageData = PageDataManager.instance(xFID).getPageData( typeId[0], recordName );
                        }
                        else
                            pageData = PageDataManager.instance(xFID).getPageData( dataProvider );
                    }
                    else
                    {
                        if ( dataProvider.contains( "=" ) )
                        {
                            String[] aliasMap = dataProvider.split( "=" );

                            String realName = aliasMap[ 0 ];
                            String alias = aliasMap[ 1 ];
                            
                            pageData = PageDataManager.instance(xFID).getPageData( realName );
                            
                            if ( log.isInfoEnabled() )
                                log.info( "Adding Alias " + alias + " for " + realName );
                            dataMap.put( alias, pageData );
                        }
                        else
                            pageData = PageDataManager.instance(xFID).getPageData( dataProvider );
                    }
                    if ( pageData == null )
                    {
                        log.fatal( "Invalid page data value specified.  Ensure that [" + dataProvider + "] exists in your page data definition" );
                        throw new DataConfigurationException( dataProvider, null );
                    }

                    if ( log.isInfoEnabled() )
                        log.info( "Adding " + dataProvider + " as " + pageData );
                    dataMap.put( dpMe, pageData );
                }
            }

            //
            // If there was a looped driver, then add that
            //
            if ( log.isInfoEnabled() )
                log.info( Thread.currentThread().getName() + ": Configuring Data Drivers" );
            if ( testName.getDataDriven() != null )
            {
                dataMap.put( test.getDataDriver(), testName.getDataDriven() );
            }

            //
            // Create a new context map and pass it along to all of the steps
            //
            testStarted = true;
            
            if ( log.isInfoEnabled() )
                log.info( Thread.currentThread().getName() + ": Alerting Listeners" );
            if ( !KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).notifyBeforeTest( webDriver, test, contextMap, dataMap, pageMap, sC, executionContext ) )
            {
                log.warn( "Test was skipped due to a failed test notification listener" );
                contextMap.put( "XF_TEST_STATUS", TestStatus.SKIPPED.name() );
                executionContext.completeTest( TestStatus.SKIPPED, new FilteredException( "Test was skipped due to a failed test notification listener" ) );
                return executionContext;
            }
            
            String xFID = webDriver.getExecutionContext().getxFID();
            
            if ( log.isInfoEnabled() )
                log.info( Thread.currentThread().getName() + ": Processing Initialization" );
            if ( DeviceManager.instance( xFID ).getInitializationName() != null )
            {
                KeyWordTest initTest = KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).getTest( DeviceManager.instance( xFID ).getInitializationName() );
                if ( initTest != null )
                {
                    if ( !DeviceManager.instance( xFID ).isDeviceInitialized( webDriver.getPopulatedDevice() ) )
                    {
                        logConsole( "Execution Initialization Method " + DeviceManager.instance( xFID ).getInitializationName() + " on " + webDriver.getPopulatedDevice().getEnvironment() + "(" + webDriver.getPopulatedDevice().getDeviceName() + ")" );
                        executionContext.startStep( new SyntheticStep( "Device Initialization", "CALL2" ), contextMap, dataMap );
                        returnValue = initTest.executeTest( webDriver, contextMap, dataMap, pageMap, sC, executionContext );
                        DeviceManager.instance( xFID ).setDeviceInitialized( webDriver.getPopulatedDevice() );
                        executionContext.completeStep( returnValue ? StepStatus.SUCCESS : StepStatus.FAILURE, null, null );
                    }  
                }
            }
            
            if ( log.isInfoEnabled() )
                log.info( Thread.currentThread().getName() + ": Executing Test=" + returnValue );
            if ( returnValue )
            {
                returnValue = test.executeTest( webDriver, contextMap, dataMap, pageMap, sC, executionContext );
                
                if ( !returnValue )
                {
                	if ( executionContext.getStepException() != null && executionContext.getStepList().isEmpty() )
                	{
                		contextMap.put( "XF_TEST_STATUS", TestStatus.FAILED.name() );
                        executionContext.startStep( new SyntheticStep( test == null ? testName.getTestName() : test.getName(), "TEST" ), contextMap, dataMap );
                        executionContext.completeStep( StepStatus.FAILURE, executionContext.getStepException(), null );
                        executionContext.completeTest( TestStatus.FAILED, null );
                        return executionContext;
                	}
                }
                
            }
            
            contextMap.put( "XF_TEST_STATUS", returnValue ? TestStatus.PASSED.name() : TestStatus.FAILED.name() );
            executionContext.completeTest( returnValue ? TestStatus.PASSED : TestStatus.FAILED, null );

            return executionContext;

        }
        catch( FlowException e )
        {
            if ( !e.isSuccess() )
            {
                
                executionContext.startStep( new SyntheticStep( test.getName(), "TEST" ), contextMap, dataMap );
                executionContext.completeStep( StepStatus.FAILURE, e, null );
                executionContext.completeTest( TestStatus.FAILED, e );
                contextMap.put( "XF_TEST_STATUS", TestStatus.FAILED.name() );
                log.info( "Error executing Test " + testName, e );
                
            }
            else
            {
                executionContext.completeStep( StepStatus.SUCCESS, null, null );
                executionContext.completeTest( TestStatus.PASSED, null );
                contextMap.put( "XF_TEST_STATUS", TestStatus.PASSED.name() );
            }
            return executionContext;
        }
        catch ( XFramiumException e )
        {
            contextMap.put( "XF_TEST_STATUS", TestStatus.FAILED.name() );
            executionContext.startStep( new SyntheticStep( test == null ? testName.getTestName() : test.getName(), "TEST" ), contextMap, dataMap );
            executionContext.completeStep( StepStatus.FAILURE, e, null );
            executionContext.completeTest( TestStatus.FAILED, null );

            log.info( "Error executing Test " + testName, e );
            return executionContext;
        }
        catch ( Throwable e )
        {
            contextMap.put( "XF_TEST_STATUS", TestStatus.FAILED.name() );
            executionContext.startStep( new SyntheticStep( test == null ? testName.getTestName() : test.getName(), "TEST" ), contextMap, dataMap );
            executionContext.completeStep( StepStatus.FAILURE, new ScriptException( e.getMessage() ), null );
            executionContext.completeTest( TestStatus.FAILED, null );
            log.info( "Error executing Test " + testName, e );
            return executionContext;
        }
        
        finally
        {
            try
            {
              
              
                if ( !contextMap.containsKey( "XF_TEST_STATUS" ) )
                    contextMap.put( "XF_TEST_STATUS", "UNKNOWN" );
                if ( testStarted )
                    KeyWordDriver.instance( ( (DeviceWebDriver) webDriver ).getxFID() ).notifyAfterTest( webDriver, test, contextMap, dataMap, pageMap, contextMap.get( "XF_TEST_STATUS" ).equals( TestStatus.PASSED.name() ), sC, executionContext );
                
                for ( String key : dataMap.keySet() )
                {
                    PageDataManager.instance(xFID).putPageData( dataMap.get( key ) );
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private void logConsole( String message )
    {
        StringBuilder reportData = new StringBuilder();
        
        reportData.append( "**********************************************************************************************************************\r\n" );
        reportData.append( message ).append( "\r\n" );
        reportData.append( "**********************************************************************************************************************\r\n" );
        
        log.warn( reportData.toString() );
    }
    
    /**
     * Gets the test names.
     *
     * @return the test names
     */
    public String[] getTestNames()
    {
        return testList.toArray( new String[0] );
    }

    /**
     * Gets the test names.
     *
     * @param useNames
     *            the use names
     * @return the test names
     */
    public String[] getTestNames( String useNames )
    {
        if ( useNames == null || useNames.isEmpty() )
            return getTestNames();

        List<String> useList = new ArrayList<String>( 5 );

        String[] nameArray = useNames.split( "," );

        for ( String name : nameArray )
        {
            if ( testMap.containsKey( name ) )
                useList.add( name );
        }

        return useList.toArray( new String[0] );
    }
    
    public String getProperty( String propertyName )
    {
      String returnValue = configProperties.getProperty( propertyName );
      
      if ( returnValue == null )
        returnValue = System.getProperty( propertyName );
      
      return returnValue;
    }

    public Properties getConfigProperties()
    {
        return configProperties;
    }

    public void setConfigProperties( Properties val )
    {
        configProperties = val;
    }

}

