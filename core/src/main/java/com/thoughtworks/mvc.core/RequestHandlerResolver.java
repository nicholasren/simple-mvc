package com.thoughtworks.mvc.core;

import com.thoughtworks.di.core.Injector;
import com.thoughtworks.utils.Lang;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

public class RequestHandlerResolver {

    private final Injector container;
    private final URLMapping urlMapping;
    private final FreeMarkerViewResolver viewResolver;

    private RequestHandlerResolver(Injector container, String packageName, String contextPath, String templatePath) {
        this.container = container;
        this.urlMapping = URLMapping.load(packageName, contextPath);
        this.viewResolver = FreeMarkerViewResolver.create(templatePath);
    }

    public static RequestHandlerResolver create(Injector container, String packageName, String contextPath, String templatePath) {
        return new RequestHandlerResolver(container, packageName, contextPath, templatePath);
    }

    public RequestHandler resolve(HttpServletRequest request) {

        ActionInfo actionInfo = urlMapping.get(request.getRequestURI());

        if (null == actionInfo) {
            throw Lang.makeThrow("can not find action for requested URI %s.", request.getRequestURI());
        }

        Controller controller = (Controller) container.get(actionInfo.getControllerClass());

        Object param = extractParam(request, actionInfo);

        return new RequestHandler(viewResolver, controller,
                actionInfo.getMethod(), param);
    }

    private Object extractParam(HttpServletRequest request, ActionInfo actionInfo) {
        RequiredParam requiredParam = actionInfo.getRequiredParam();
        Object param = null;
        if (null != requiredParam) {
            param = request.getParameter(requiredParam.getName());
        }

        return param;

    }

}
