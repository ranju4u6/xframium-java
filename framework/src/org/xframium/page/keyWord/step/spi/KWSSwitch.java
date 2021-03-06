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
package org.xframium.page.keyWord.step.spi;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.xframium.container.SuiteContainer;
import org.xframium.page.Page;
import org.xframium.page.data.PageData;
import org.xframium.page.keyWord.KeyWordParameter;
import org.xframium.page.keyWord.step.AbstractKeyWordStep;
import org.xframium.reporting.ExecutionContextTest;

// TODO: Auto-generated Javadoc
/**
 * The Class KWSFork.
 */
public class KWSSwitch extends AbstractKeyWordStep
{
	private static Pattern STEP_PATTERN = Pattern.compile( "Step\\s(\\d)" );
	/**
	 * Instantiates a new KWS fork.
	 */
	public KWSSwitch()
	{
		setFork( true );
		orMapping = false;
		category = "Flow Control";
		kwName = "Switch Statement";
    kwDescription = "Allows the script to execute a numbered step given the condition";
    featureId = 49;
		
	}
	/* (non-Javadoc)
	 * @see com.perfectoMobile.page.keyWord.step.AbstractKeyWordStep#_executeStep(com.perfectoMobile.page.Page, org.openqa.selenium.WebDriver, java.util.Map, java.util.Map)
	 */
	@Override
	public boolean _executeStep( Page pageObject, WebDriver webDriver, Map<String, Object> contextMap, Map<String, PageData> dataMap, Map<String, Page> pageMap, SuiteContainer sC, ExecutionContextTest executionContext ) throws Exception
	{
		validateParameters(new String[] { "Value", "Step 1" } );
		
		String compareTo = getParameterValue( getParameter( "Value" ), contextMap, dataMap, executionContext.getxFID() );
		
		for ( KeyWordParameter p : getParameterList() )
		{
			if ( compareTo.equals( getParameterValue( p, contextMap, dataMap, executionContext.getxFID() ) ) )
			{
				Matcher stepMatcher = STEP_PATTERN.matcher( p.getName() );
				if ( stepMatcher.find() )
				{
					int stepNumber = Integer.parseInt( stepMatcher.group( 1 ) ) - 1;
					return getStepList().get( stepNumber ).executeStep( pageObject, webDriver, contextMap, dataMap, pageMap, sC, executionContext );
				}
			}
				
		}
		
		for ( KeyWordParameter p : getParameterList() )
		{
			if ( "Default".equals( getParameterValue( p, contextMap, dataMap, executionContext.getxFID() ) ) )
			{
				Matcher stepMatcher = STEP_PATTERN.matcher( p.getName() );
				if ( stepMatcher.find() )
				{
					int stepNumber = Integer.parseInt( stepMatcher.group( 1 ) ) - 1;
					return getStepList().get( stepNumber ).executeStep( pageObject, webDriver, contextMap, dataMap, pageMap, sC, executionContext );
				}
			}
		}
		
		return false;
		
		
	}

}
