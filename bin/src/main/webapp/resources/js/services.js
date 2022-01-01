'use strict';

myapp.service('Session', function () {
    this.create = function (data) {
        this.id = data.id;
        this.login = data.login;
        this.firstName = data.firstName;
        this.lastName = data.surname;
        this.email = data.email;
        this.userRoles = [];
        angular.forEach(data.authorities, function (value, key) {
            this.push(value.name);
        }, this.userRoles);
    };
    this.invalidate = function () {
        this.id = null;
        this.login = null;
        this.firstName = null;
        this.lastName = null;
        this.email = null;
        this.userRoles = null;
    };
    return this;
});


myapp.service('AuthSharedService', function ($rootScope, $http, $resource, $location, authService, Session) {
    return {
        login: function (userName, password, rememberMe) {
            var config = {
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            };
            $http.get('user/session')
                .then(function (response) {
                    $http.post('authenticate', $.param({
                        username: userName,
                        password: password,
                        rememberme: rememberMe
                    }), config)
                        .success(function (data, status, headers, config) {
                            authService.loginConfirmed(data);
                        })
                        .error(function (data, status, headers, config) {
                            $rootScope.authenticationError = true;
                            $rootScope.authenticationErrorMessage = data.error.message;
                            Session.invalidate();
                        });
                });
        },
        signup: function (firstName, lastName, email, password) {
            var config = {
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {'Content-Type': 'application/json'}
            };
            $http.get('user/session')
                .then(function (response) {
                    $http.post('user/createaccount', {
                        firstName: firstName,
                        surname: lastName,
                        email: email,
                        password: password
                    }, config)
                        .success(function (data, status, headers, config) {
                            if(CONFIRM_REGISTRATION) {
                                $rootScope.loginMessage = data.message;
                                $rootScope.hasLoginMessage = true;
                                $location.path('/login').replace();
                            } else {
                                authService.loginConfirmed(data);
                            }
                        })
                        .error(function (data, status, headers, config) {
                            $rootScope.authenticationError = true;
                            $rootScope.authenticationErrorMessage = data.error.message;
                            Session.invalidate();
                        });
                });
        },
        processRegistrationConfirmUrl: function (token) {
            $rootScope.loadingExternalUrl = true;
            var config = {
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            };
            var promise;
            promise = $http.post('user/registrationConfirm', $.param({token: token}), config);
            promise.then(function(resp){
                return resp.data;
            });
            return promise;

        },
        resetpassword: function (email) {
            var config = {
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            };
            $http.get('user/session')
                .then(function (response) {
                    return $http.post('user/resetPassword', $.param({email: email}), config)
                        .success(function (data, status, headers, config) {
                            $rootScope.hasPasswordResetMessage = true;
                            $rootScope.passwordResetMessage = data.message;
                        })
                        .error(function (data, status, headers, config) {
                            $rootScope.passwordResetError = true;
                            $rootScope.passwordResetErrorMessage = data.message;
                        });
                });
        },
        processResetpasswordUrl: function (id, token) {
            $rootScope.loadingExternalUrl = true;
            var config = {
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            };
            var promise;
            promise = $http.post('user/processResetPasswordUrl', $.param({id: id, token: token}), config);
            promise.then(function(resp){
                return resp.data;
            });
            return promise;

        },
        changepassword: function (password) {
            var config = {
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            };

            $http.post('user/changePassword', $.param({
                password: password
            }), config)
                .success(function (data, status, headers, config) {
                    if(DEBUG) {
                        $rootScope.loginMessage = data.message;
                        $rootScope.hasLoginMessage = true;
                        $location.path('/login').replace();
                    }
                })
                .error(function (data, status, headers, config) {
                    $rootScope.passwordError = true;
                });
        },
        getAccount: function () {
            $rootScope.loadingAccount = true;
            $http.get('security/account')
                .then(function (response) {
                    if(response.data.login == undefined) {
                        $rootScope.authenticated = false;
                        $rootScope.account = null;
                        Session.invalidate();
                        //authService.loginCancelled();
                    } else {
                        authService.loginConfirmed(response.data);
                    }
                });
        },
        isAuthorized: function (authorizedRoles) {
            if (!angular.isArray(authorizedRoles)) {
                if (authorizedRoles == '*') {
                    return true;
                }
                authorizedRoles = [authorizedRoles];
            }
            var isAuthorized = false;
            angular.forEach(authorizedRoles, function (authorizedRole) {
                var authorized = (!!Session.login &&
                Session.userRoles.indexOf(authorizedRole) !== -1);
                if (authorized || authorizedRole == '*') {
                    isAuthorized = true;
                }
            });
            return isAuthorized;
        },
        logout: function () {
            $rootScope.authenticationError = false;
            $rootScope.authenticated = false;
            $rootScope.account = null;
            $http.post('logout');
            Session.invalidate();
            authService.loginCancelled();
        }
    };
});

myapp.service('HomeService', function ($log, $resource) {
    return {
        getTechno: function () {
            var userResource = $resource('resources/json/techno.json', {}, {
                query: {method: 'GET', params: {}, isArray: true}
            });
            return userResource.query();
        }
    }
});


myapp.service('UsersService', function ($log, $resource) {
    return {
        getAll: function () {
            var userResource = $resource('users', {}, {
                query: {method: 'GET', params: {}, isArray: true}
            });
            return userResource.query();
        }
    }
});


myapp.service('TokensService', function ($log, $resource) {
    return {
        getAll: function () {
            var tokensResource = $resource('security/tokens', {}, {
                query: {method: 'GET', params: {}, isArray: true}
            });
            return tokensResource.query();
        }
    }
});


