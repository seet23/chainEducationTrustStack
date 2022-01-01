'use strict';

var DEBUG = true;
var CONFIRM_REGISTRATION = true;
var myapp = angular
    .module('myApp', ['ngResource', 'ngRoute', 'swaggerUi', 'http-auth-interceptor', 'ngAnimate', 'angular-spinkit']);

myapp.constant('USER_ROLES', {
    all: '*',
    admin: 'admin',
    user: 'user'
});


myapp.config(function ($routeProvider, USER_ROLES) {

    $routeProvider.when("/home", {
        templateUrl: "partials/home.html",
        controller: 'HomeController',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/', {
        redirectTo: '/home'
    }).when('/users', {
        templateUrl: 'partials/users.html',
        controller: 'UsersController',
        access: {
            loginRequired: true,
            authorizedRoles: [USER_ROLES.admin]
        }
    }).when('/apiDoc', {
        templateUrl: 'partials/apiDoc.html',
        controller: 'ApiDocController',
        access: {
            loginRequired: true,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/tokens', {
        templateUrl: 'partials/tokens.html',
        controller: 'TokensController',
        access: {
            loginRequired: true,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/login', {
        templateUrl: 'partials/login.html',
        controller: 'LoginController',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/resetpassword', {
        templateUrl: 'partials/resetpassword.html',
        controller: 'ResetPasswordController',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/resetpassword/:id/:token', {
        templateUrl: 'partials/resetpassword.html',
        controller: 'ResetPasswordUrlController',
        resolve: { resetPasswordResponse :
                    function($route, AuthSharedService) {
                        return AuthSharedService.processResetpasswordUrl($route.current.params.id, $route.current.params.token);
                    }},
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/changepassword', {
        templateUrl: 'partials/changepassword.html',
        controller: 'ChangePasswordController',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/signup', {
        templateUrl: 'partials/signup.html',
        controller: 'SignupController',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/registrationconfirm/:token', {
        templateUrl: 'partials/registrationconfirm.html',
        controller: 'RegistrationConfirmController',
        resolve: { registrationConfirmResponse :
                    function($route, AuthSharedService) {
                        return AuthSharedService.processRegistrationConfirmUrl($route.current.params.token);
                    }},
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when('/loading', {
        templateUrl: 'partials/loading.html',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when("/logout", {
        template: " ",
        controller: "LogoutController",
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).when("/error/:code", {
        templateUrl: "partials/error.html",
        controller: "ErrorController",
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    }).otherwise({
        redirectTo: '/error/404',
        access: {
            loginRequired: false,
            authorizedRoles: [USER_ROLES.all]
        }
    });
});

myapp.run(function ($rootScope, $location, $http, AuthSharedService, Session, USER_ROLES, $q, $timeout) {

    $rootScope.$on('$routeChangeStart', function (event, next) {
        if(next.originalPath === "/login" && $rootScope.authenticated) {
            event.preventDefault();
        } else if (next.access && next.access.loginRequired && !$rootScope.authenticated) {
            event.preventDefault();
            $rootScope.$broadcast("event:auth-loginRequired", {});
        } else if (next.access && !AuthSharedService.isAuthorized(next.access.authorizedRoles)) {
            event.preventDefault();
            $rootScope.$broadcast("event:auth-forbidden", {});
        }
    });

    $rootScope.$on('$routeChangeSuccess', function (scope, next, current) {
        $rootScope.$evalAsync(function () {
            $.material.init();
        });
    });

    // Call when the the client is confirmed
    $rootScope.$on('event:auth-loginConfirmed', function (event, data) {
        $rootScope.loadingAccount = false;
        var nextLocation = ($rootScope.requestedUrl ? $rootScope.requestedUrl : "/home");
        var delay = ($location.path() === "/loading" ? 1500 : 0);

        $timeout(function () {
            Session.create(data);
            $rootScope.account = Session;
            $rootScope.authenticated = true;
            $location.path(nextLocation).replace();
        }, delay);

    });

    // Call when the 401 response is returned by the server
    $rootScope.$on('event:auth-loginRequired', function (event, data) {
        if ($rootScope.loadingAccount && data.status !== 401) {
            $rootScope.requestedUrl = $location.path()
            $location.path('/loading');
        } else if($rootScope.loadingExternalUrl) {
            $rootScope.loadingExternalUrl = false;
        } else {
            Session.invalidate();
            $rootScope.authenticated = false;
            $rootScope.loadingAccount = false;
            $location.path('/home');
        }
    });

    // Call when the 403 response is returned by the server
    $rootScope.$on('event:auth-forbidden', function (rejection) {
        $rootScope.$evalAsync(function () {
            $location.path('/error/403').replace();
        });
    });

    // Call when the user logs out
    $rootScope.$on('event:auth-loginCancelled', function () {
        $location.path('/home').replace();
    });

    // Get already authenticated user account
    $http.get('user/session')
        .then(function (response) {
            AuthSharedService.getAccount();
    });


});





