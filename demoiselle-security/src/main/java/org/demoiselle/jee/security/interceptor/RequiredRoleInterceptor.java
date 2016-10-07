/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.List;

import java.util.logging.Logger;
import javax.inject.Inject;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;
import org.demoiselle.jee.core.util.ResourceBundle;
import org.demoiselle.jee.security.annotation.RequiredRole;
import org.demoiselle.jee.core.interfaces.security.SecurityContext;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
import org.demoiselle.jee.security.message.DemoiselleSecurityMessages;

/**
 * <p>
 * Intercepts calls with {@code @RequiredRole} annotations.
 * </p>
 *
 * @author SERPRO
 */
@RequiredRole(value = "")
@Interceptor
@Priority(Priorities.AUTHORIZATION)
public class RequiredRoleInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private DemoisellePrincipal loggedUser;

    @Inject
    private DemoiselleSecurityMessages bundle;

    @Inject
    private Logger logger;

    /**
     * <p>
     * Gets the value property of {@code @RequiredRole}. Delegates to
     * {@code SecurityContext} check role. If the user has the required role it
     * executes the mehtod, otherwise throws an exception. Returns what is
     * returned from the intercepted method. If the method's return type is
     * {@code void} returns {@code null}.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return what is returned from the intercepted method. If the method's
     * return type is {@code void} returns {@code null}.
     * @throws Exception if there is an error during the role check or during
     * the method's processing.
     */
    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        List<String> roles = getRoles(ic);

        List<String> userRoles = new ArrayList<>();
        
        if (!securityContext.isLoggedIn()) {
            throw new DemoiselleSecurityException(bundle.userNotAuthenticated(), UNAUTHORIZED.getStatusCode());
        }

        for (String role : roles) {
            if (securityContext.hasRole(role)) {
                userRoles.add(role);
            }
        }

        if (userRoles.isEmpty()) {
            throw new DemoiselleSecurityException(bundle.doesNotHaveRole(roles.toString()), UNAUTHORIZED.getStatusCode());
        }

        return ic.proceed();
    }

    /**
     * <p>
     * Returns the value defined in {@code @RequiredRole} annotation.
     * </p>
     *
     * @param ic the {@code InvocationContext} in which the method is being
     * called.
     * @return the value defined in {@code @RequiredRole} annotation.
     */
    private List<String> getRoles(InvocationContext ic) {
        String[] roles = {};

        if (ic.getMethod().getAnnotation(RequiredRole.class) == null) {
            roles = ic.getTarget().getClass().getAnnotation(RequiredRole.class).value();
        } else {
            roles = ic.getMethod().getAnnotation(RequiredRole.class).value();
        }

        return asList(roles);
    }

}
