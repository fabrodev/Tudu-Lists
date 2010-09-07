package tudu.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tudu.domain.model.Role;
import tudu.domain.model.User;
import tudu.service.UserManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

/**
 * An implementation of Spring Security's UserDetailsService.
 * 
 * @author Julien Dubois
 */
@Service("userDetailsService")
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Log log = LogFactory.getLog(UserDetailsServiceImpl.class);

    @Autowired
    private UserManager userManager;

    /**
     * Load a user for Spring Security.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException, DataAccessException {
        login = login.toLowerCase();
        if (log.isDebugEnabled()) {
            log.debug("Security verification for user '" + login + "'");
        }
        User user;
        try {
            user = userManager.findUser(login);
        } catch (ObjectRetrievalFailureException orfe) {
            throw new UsernameNotFoundException("User '" + login
                    + "' could not be found.");
        }
        user.setLastAccessDate(Calendar.getInstance().getTime());
        userManager.updateUser(user);

        Set<Role> roles = user.getRoles();

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (Role role : roles) {
            authorities.add(new GrantedAuthorityImpl(role.getRole()));
        }

        return new org.springframework.security.core.userdetails.User(login,
                user.getPassword(), user.isEnabled(), true, true, true,
                authorities);
    }
}
