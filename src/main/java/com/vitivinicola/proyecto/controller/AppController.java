package com.vitivinicola.proyecto.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.tree.RowMapper;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.vitivinicola.proyecto.model.User;
import com.vitivinicola.proyecto.model.UserProfile;
import com.vitivinicola.proyecto.model.Vinias;
import com.vitivinicola.proyecto.service.UserProfileService;
import com.vitivinicola.proyecto.service.UserService;
import com.vitivinicola.proyecto.service.ViniasService;

@Controller
@Transactional
@RequestMapping("/")
@SessionAttributes("roles")
public class AppController {

	@Autowired
	UserService userService;

	@Autowired
	ViniasService viniasService;

	@Autowired
	UserProfileService userProfileService;

	@Autowired
	MessageSource messageSource;

	@Autowired
	PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;

	@Autowired
	AuthenticationTrustResolver authenticationTrustResolver;

	@RequestMapping(value = { "/", "vistaHome" }, method = RequestMethod.GET)
	public String home(ModelMap model) {

		return "home";
	}

	@RequestMapping(value = { "/list" }, method = RequestMethod.GET)
	public String listUsers(ModelMap model) {

		List<User> users = userService.findAllUsers();
		model.addAttribute("users", users);
		model.addAttribute("loggedinuser", getPrincipal());
		return "userslist";
	}

	@RequestMapping(value = { "/newuser" }, method = RequestMethod.GET)
	public String newUser(ModelMap model) {
		User user = new User();
		model.addAttribute("user", user);
		model.addAttribute("edit", false);
		model.addAttribute("loggedinuser", getPrincipal());
		return "registration";
	}

	@RequestMapping(value = { "/newuser" }, method = RequestMethod.POST)
	public String saveUser(@Valid User user, BindingResult result, ModelMap model) {

		if (result.hasErrors()) {

			if (user.getUserProfiles().isEmpty()) {

				user.getUserProfiles().add(new UserProfile(1, "USER"));
				if (!userService.isUserSSOUnique(user.getId(), user.getSsoId())) {
					FieldError ssoError = new FieldError("user", "ssoId", messageSource.getMessage("non.unique.ssoId",
							new String[] { user.getSsoId() }, Locale.getDefault()));
					result.addError(ssoError);
					return "registration";
				}

				userService.saveUser(user);
				model.addAttribute("success",
						"Estimado Usuario " + user.getFirstName() + " Su Registro Fue Realizado Correctamente");
				model.addAttribute("loggedinuser", getPrincipal());

			}

			// userService.saveUser(user);

			return "registrationsuccess";
		}

		return "registrationsuccess";
	}

	/**
	 * This method will provide the medium to update an existing user.
	 */
	@RequestMapping(value = { "/edit-user-{ssoId}" }, method = RequestMethod.GET)
	public String editUser(@PathVariable String ssoId, ModelMap model) {
		User user = userService.findBySSO(ssoId);
		model.addAttribute("user", user);
		model.addAttribute("edit", true);
		model.addAttribute("loggedinuser", getPrincipal());
		return "registration";
	}

	/**
	 * This method will be called on form submission, handling POST request for
	 * updating user in database. It also validates the user input
	 */
	@RequestMapping(value = { "/edit-user-{ssoId}" }, method = RequestMethod.POST)
	public String updateUser(@Valid User user, BindingResult result, ModelMap model, @PathVariable String ssoId) {

		if (result.hasErrors()) {

			if (user.getUserProfiles().isEmpty()) {

				user.getUserProfiles().add(new UserProfile(1, "USER"));
				if (!userService.isUserSSOUnique(user.getId(), user.getSsoId())) {
					FieldError ssoError = new FieldError("user", "ssoId", messageSource.getMessage("non.unique.ssoId",
							new String[] { user.getSsoId() }, Locale.getDefault()));
					result.addError(ssoError);
					return "registration";
				}

				userService.saveUser(user);
				model.addAttribute("success",
						"Estimado Usuario " + user.getFirstName() + " Su Registro Fue Realizado Correctamente");
				model.addAttribute("loggedinuser", getPrincipal());

			}

			// userService.saveUser(user);

			return "registrationsuccess";
		}

		return "registrationsuccess";
	}

	/**
	 * This method will delete an user by it's SSOID value.
	 */
	@RequestMapping(value = { "/delete-user-{ssoId}" }, method = RequestMethod.GET)
	public String deleteUser(@PathVariable String ssoId) {
		userService.deleteUserBySSO(ssoId);
		return "redirect:/list";
	}
	@RequestMapping(value = { "/delete-vinia-{id}" }, method = RequestMethod.GET)
	public String deleteVinia(@PathVariable int id) {
		viniasService.deleteVinia(id);
		return "redirect:/listVinias";
	}

	/**
	 * This method will provide UserProfile list to views
	 */
	@ModelAttribute("roles")
	public List<UserProfile> initializeProfiles() {
		return userProfileService.findAll();
	}

	/**
	 * This method handles Access-Denied redirect.
	 */
	@RequestMapping(value = "/Access_Denied", method = RequestMethod.GET)
	public String accessDeniedPage(ModelMap model) {
		model.addAttribute("loggedinuser", getPrincipal());
		return "accessDenied";
	}

	/**
	 * This method handles login GET requests. If users is already logged-in and
	 * tries to goto login page again, will be redirected to list page.
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginPage(ModelMap model) {
		if (isCurrentAuthenticationAnonymous()) {
			model.addAttribute("edit", true);
			return "login";
		} else {
			return "redirect:/logout";
		}
	}

	/**
	 * This method handles logout requests. Toggle the handlers if you are
	 * RememberMe functionality is useless in your app.
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			// new SecurityContextLogoutHandler().logout(request, response, auth);
			persistentTokenBasedRememberMeServices.logout(request, response, auth);
			SecurityContextHolder.getContext().setAuthentication(null);
		}
		return "redirect:/login?logout";
	}

	@RequestMapping(value = { "/listVinias" }, method = RequestMethod.GET)
	public String addDocuments(ModelMap model) {
		System.out.println("id es " + getIdPrincipalLogin());
		int id;
		User user = userService.findById(getIdPrincipalLogin());
		id = user.getId();
		model.addAttribute("user", user);
		List<Vinias> documents = viniasService.findAllVinias();
		List<Vinias> viniasProductor = new ArrayList<Vinias>();
		for (int index = 0; index < documents.size(); index++) {
			if (documents.get(index).getCliente().getId() == id) {
				viniasProductor.add(documents.get(index));
			}
		}

		model.addAttribute("documents", viniasProductor);

		return "viniasList";
	}
	@RequestMapping(value = { "/listViniasTotal" }, method = RequestMethod.GET)
	public String listViniasTotal(ModelMap model) {
		List<Vinias> listTotal = viniasService.findAllVinias();
		model.addAttribute("documents",listTotal);
		return "viniasList";
	}

	@RequestMapping(value = { "/edit-vinias-{id}" }, method = RequestMethod.GET)
	public String editVinia(@PathVariable int id, ModelMap model) {
		Vinias vinia = viniasService.findById(id);
		model.addAttribute("vinias", vinia);
		model.addAttribute("edit", false);
		model.addAttribute("loggedinuser", getPrincipal());
		return "formVinias";
	}

	@RequestMapping(value = { "/edit-vinias-{id}" }, method = RequestMethod.POST)
	public String updateVinia(@Valid Vinias vinia, BindingResult result, ModelMap model, @PathVariable int id) {

		if (result.hasErrors()) {
			return "formVinias";
		}

		viniasService.updateVinia(vinia);
		model.addAttribute("success", "Estimado su viña con nombre " + vinia.getName() + " a sido actualizada");
		model.addAttribute("loggedinuser", getPrincipal());

		return "registrationsuccess";
	}

	private int getIdPrincipalLogin() {
		int userid = 0;
		String userName = null;
		User usuario = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails) principal).getUsername().trim();
			usuario = userService.findBySSO(userName);
			userid = usuario.getId();
		} else {
			userName = principal.toString();
			usuario = userService.findBySSO(userName);
			userid = usuario.getId();
		}
		return userid;
	}

	@RequestMapping(value = { "/newVinias" }, method = RequestMethod.GET)
	public String newVinias(ModelMap model) {
		Vinias vinias = new Vinias();
		model.addAttribute("vinias", vinias);
		model.addAttribute("edit", true);
		model.addAttribute("loggedinuser", getPrincipal());
		return "formVinias";
	}

	@RequestMapping(value = { "/newVinias" }, method = RequestMethod.POST)
	public String saveVinias(@Valid Vinias vinias, BindingResult result, ModelMap model) {

		if (result.hasErrors()) {

			return "formVinias";
		}
		System.out.println("Entre a registrar");
		User user = userService.findById(getIdPrincipalLogin());
		System.out.println("obtube el id " + getIdPrincipalLogin());
		Vinias viniasAux = vinias;
		System.out.println("obtuve la viña" + viniasAux.getName());
		// user.getListaVinias().add(viniasAux);
		viniasAux.setCliente(user);
		viniasService.saveVinia(viniasAux);
		model.addAttribute("success", "Estimado Usuario " + " Su Registro De viñas a sido agregada satisfactoreamente");

		return "home";
	}

	/**
	 * This method returns the principal[user-name] of logged-in user.
	 */
	private String getPrincipal() {
		String userName = null;

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails) principal).getUsername();
		} else {
			userName = principal.toString();
		}
		return userName;
	}

	/**
	 * This method returns true if users is already authenticated [logged-in], else
	 * false.
	 */
	@ModelAttribute("IniClo")
	private boolean isCurrentAuthenticationAnonymous() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authenticationTrustResolver.isAnonymous(authentication);
	}

}