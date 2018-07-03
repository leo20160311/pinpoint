/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.jboss.JbossServletRequestListener;
import org.apache.catalina.core.StandardContext;

import javax.servlet.ServletRequestListener;

/**
 * @author jaehong.kim
 */
public class StandardContextStartInternalInterceptor implements AroundInterceptor {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    private ServletRequestListener servletRequestListener;

    public StandardContextStartInternalInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.servletRequestListener = new JbossServletRequestListener(this.traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
        // Add servlet request listener. Servlet 2.4
        try {
            if (target instanceof StandardContext) {
                final StandardContext standardContext = (StandardContext) target;
                standardContext.addApplicationListenerInstance(this.servletRequestListener);
                if (isDebug) {
                    logger.debug("Add servlet request listener. servletRequestListener={}", this.servletRequestListener);
                }
            } else {
                logger.info("Failed to add servlet request listener. The target object is not an org.apache.catalina.core.StandardContext implementation. target={}", target);
            }
        } catch (Exception e) {
            logger.info("Failed to add servlet request listener. target={}, servletRequestListener={}", target, this.servletRequestListener, e);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}