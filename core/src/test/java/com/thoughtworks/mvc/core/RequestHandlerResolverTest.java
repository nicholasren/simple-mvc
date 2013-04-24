package com.thoughtworks.mvc.core;

import com.example.controller.UserController;
import com.example.model.User;
import com.example.service.UserService;
import com.thoughtworks.di.core.Injector;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestHandlerResolverTest {

    private Injector container;
    private RequestHandlerResolver resolver;

    @Before
    public void setUp() {
        container = Injector.create("com.example");

        String templatePath = this.getClass().getResource("/").getPath();

        resolver = RequestHandlerResolver.create(container, "com.example", "/sample", templatePath);
    }

    @Test
    public void should_find_controller_by_collection_url() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sample/user/index");
        when(request.getMethod()).thenReturn("GET");

        RequestHandler handler = resolver.resolve(request);

        assertThat(handler.getController(), instanceOf(UserController.class));
        assertThat(handler.getActionName(), equalTo("index"));
    }

    //REST-ful url mapping
    @Test
    public void should_find_controller_by_member_url() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sample/user/show");
        when(request.getMethod()).thenReturn("GET");

        RequestHandler requestHandler = resolver.resolve(request);

        assertThat(requestHandler.getController(), instanceOf(UserController.class));
        assertThat(requestHandler.getActionName(), equalTo("show"));
    }

    @Test
    public void should_dispatch_get_by_id_request_to_show_action() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sample/user/show");
        when(request.getMethod()).thenReturn("GET");


        RequestHandler requestHandler = resolver.resolve(request);

        assertThat(requestHandler.getController(), instanceOf(UserController.class));
        assertThat(requestHandler.getActionName(), equalTo("show"));

    }

    @Test
    public void should_dispatch_new_request_to_new_action() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sample/user/new");
        when(request.getMethod()).thenReturn("GET");


        RequestHandler requestHandler = resolver.resolve(request);

        assertThat(requestHandler.getController(), instanceOf(UserController.class));
        assertThat(requestHandler.getActionName(), equalTo("fresh"));
    }

    @Test
    public void should_inject_required_service_to_controller() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sample/user/new");
        when(request.getMethod()).thenReturn("GET");

        RequestHandler requestHandler = resolver.resolve(request);
        UserService service = ((UserController) requestHandler.getController()).getService();
        assertThat(service, notNullValue());
    }

    @Test
    public void should_inject_required_param_on_handling_request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/sample/user/show");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("id")).thenReturn("1");

        RequestHandler requestHandler = resolver.resolve(request);
        ModelAndView mv = requestHandler.handle();
        assertThat(((User) mv.getModel().get("user")).getId(), equalTo("1"));
    }

}
