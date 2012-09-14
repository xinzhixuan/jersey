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
package org.glassfish.jersey.server.wadl.internal.generators;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.wadl.WadlGenerator;
import org.glassfish.jersey.server.wadl.internal.ApplicationDescription;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * This {@link org.glassfish.jersey.server.wadl.WadlGenerator} adds the provided {@link Grammars} element to the
 * generated wadl-file.
 * <p>
 * The {@link Grammars} content can either be provided via a {@link File} ({@link #setGrammarsFile(File)}) reference or
 * via an {@link InputStream} ({@link #setGrammarsStream(InputStream)}).
 * </p>
 * <p>
 * The {@link File} should be used when using the maven-wadl-plugin for generating wadl offline,
 * the {@link InputStream} should be used when the extended wadl is generated by jersey at runtime, e.g.
 * using the {@link org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig} for configuration.
 * </p>
 * Created on: Jun 24, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class WadlGeneratorGrammarsSupport implements WadlGenerator {

    private static final Logger LOG = Logger.getLogger(WadlGeneratorGrammarsSupport.class.getName());

    private WadlGenerator _delegate;
    private File _grammarsFile;
    private InputStream _grammarsStream;
    private Grammars _grammars;
    private Boolean overrideGrammars = false;

    public WadlGeneratorGrammarsSupport() {
    }

    public WadlGeneratorGrammarsSupport(WadlGenerator delegate,
                                        Grammars grammars) {
        _delegate = delegate;
        _grammars = grammars;
    }

    public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        _delegate = delegate;
    }

    public void setOverrideGrammars(Boolean overrideGrammars) {
        this.overrideGrammars = overrideGrammars;
    }

    public String getRequiredJaxbContextPath() {
        return _delegate.getRequiredJaxbContextPath();
    }

    public void setGrammarsFile(File grammarsFile) {
        if (_grammarsStream != null) {
            throw new IllegalStateException("The grammarsStream property is already set," +
                    " therefore you cannot set the grammarsFile property. Only one of both can be set at a time.");
        }
        _grammarsFile = grammarsFile;
    }

    public void setGrammarsStream(InputStream grammarsStream) {
        if (_grammarsFile != null) {
            throw new IllegalStateException("The grammarsFile property is already set," +
                    " therefore you cannot set the grammarsStream property. Only one of both can be set at a time.");
        }
        _grammarsStream = grammarsStream;
    }

    public void init() throws IllegalStateException, JAXBException {
        if (_grammarsFile == null && _grammarsStream == null) {
            throw new IllegalStateException("Neither the grammarsFile nor the grammarsStream" +
                    " is set, one of both is required.");
        }
        _delegate.init();
        final JAXBContext c = JAXBContext.newInstance(Grammars.class);
        final Unmarshaller m = c.createUnmarshaller();
        final Object obj = _grammarsFile != null ? m.unmarshal(_grammarsFile) : m.unmarshal(_grammarsStream);
        _grammars = Grammars.class.cast(obj);
    }

    /**
     * @return application
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createApplication()
     */
    public Application createApplication() {
        final Application result = _delegate.createApplication();
        if (result.getGrammars() != null && !overrideGrammars) {
            LOG.info("The wadl application created by the delegate (" + _delegate + ") already contains a grammars element," +
                    " we're adding elements of the provided grammars file.");
            if (!_grammars.getAny().isEmpty()) {
                result.getGrammars().getAny().addAll(_grammars.getAny());
            }
            if (!_grammars.getDoc().isEmpty()) {
                result.getGrammars().getDoc().addAll(_grammars.getDoc());
            }
            if (!_grammars.getInclude().isEmpty()) {
                result.getGrammars().getInclude().addAll(_grammars.getInclude());
            }
        } else {
            result.setGrammars(_grammars);
        }
        return result;
    }

    /**
     * @param ar  abstract resource
     * @param arm abstract resource method
     * @return method
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createMethod(org.glassfish.jersey.server.model.Resource, org.glassfish.jersey.server.model.ResourceMethod)
     */
    public Method createMethod(org.glassfish.jersey.server.model.Resource ar,
                               org.glassfish.jersey.server.model.ResourceMethod arm) {
        return _delegate.createMethod(ar, arm);
    }

    /**
     * @param ar  abstract resource
     * @param arm abstract resource method
     * @return request
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createRequest(org.glassfish.jersey.server.model.Resource, org.glassfish.jersey.server.model.ResourceMethod)
     */
    public Request createRequest(org.glassfish.jersey.server.model.Resource ar,
                                 org.glassfish.jersey.server.model.ResourceMethod arm) {
        return _delegate.createRequest(ar, arm);
    }

    /**
     * @param ar abstract resource
     * @param am abstract method
     * @param p  parameter
     * @return parameter
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createParam(org.glassfish.jersey.server.model.Resource, org.glassfish.jersey.server.model.ResourceMethod, org.glassfish.jersey.server.model.Parameter)
     */
    public Param createParam(org.glassfish.jersey.server.model.Resource ar,
                             org.glassfish.jersey.server.model.ResourceMethod am, Parameter p) {
        return _delegate.createParam(ar, am, p);
    }

    /**
     * @param ar  abstract resource
     * @param arm abstract resource method
     * @param mt  media type
     * @return respresentation type
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createRequestRepresentation(org.glassfish.jersey.server.model.Resource, org.glassfish.jersey.server.model.ResourceMethod, javax.ws.rs.core.MediaType)
     */
    public Representation createRequestRepresentation(
            org.glassfish.jersey.server.model.Resource ar, org.glassfish.jersey.server.model.ResourceMethod arm, MediaType mt) {
        return _delegate.createRequestRepresentation(ar, arm, mt);
    }

    /**
     * @param ar   abstract resource
     * @param path resource path
     * @return resource
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createResource(org.glassfish.jersey.server.model.Resource, String)
     */
    public Resource createResource(org.glassfish.jersey.server.model.Resource ar, String path) {
        return _delegate.createResource(ar, path);
    }

    /**
     * @return resources
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createResources()
     */
    public Resources createResources() {
        return _delegate.createResources();
    }

    /**
     * @param ar  abstract resource
     * @param arm abstract resource method
     * @return response
     * @see org.glassfish.jersey.server.wadl.WadlGenerator#createResponses(org.glassfish.jersey.server.model.Resource, org.glassfish.jersey.server.model.ResourceMethod)
     */
    public List<Response> createResponses(org.glassfish.jersey.server.model.Resource ar,
                                          org.glassfish.jersey.server.model.ResourceMethod arm) {
        return _delegate.createResponses(ar, arm);
    }

    // ================ methods for post build actions =======================

    @Override
    public ExternalGrammarDefinition createExternalGrammar() {
        if (overrideGrammars) {
            return new ExternalGrammarDefinition();
        }
        return _delegate.createExternalGrammar();
    }

    @Override
    public void attachTypes(ApplicationDescription egd) {
        _delegate.attachTypes(egd);
    }

}
