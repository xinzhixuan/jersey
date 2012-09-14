/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.server.internal.inject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.internal.ProcessingException;
import org.glassfish.jersey.spi.StringValueReader;

/**
 * Extract value of the parameter using a single parameter value and the underlying
 * string reader.
 *
 * @param <T> extracted Java type.
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class SingleValueExtractor<T> extends AbstractStringReaderExtractor<T> implements MultivaluedParameterExtractor<T> {

    /**
     * Create new string value extractor.
     *
     * @param sr                 string value reader.
     * @param parameter          string parameter value.
     * @param defaultStringValue default string value.
     */
    public SingleValueExtractor(StringValueReader<T> sr, String parameter, String defaultStringValue) {
        super(sr, parameter, defaultStringValue);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation extracts the value of the parameter applying the underlying
     * string reader to the first value found in the list of potential multiple
     * parameter values. Any other values in the multi-value list will be ignored.
     *
     * @param parameters map of parameters.
     * @return extracted single parameter value.
     */
    @Override
    public T extract(MultivaluedMap<String, String> parameters) {
        String v = parameters.getFirst(getName());
        if (v != null) {
            try {
                return fromString(v);
            } catch (WebApplicationException ex) {
                throw ex;
            } catch (ProcessingException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ExtractorException(ex);
            }
        } else {
            return defaultValue();
        }
    }
}
