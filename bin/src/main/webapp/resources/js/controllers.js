'use strict';


myapp.controller('LoginController', function ($rootScope, $scope, AuthSharedService) {

        $rootScope.authenticationErrorMessage = '';
        $scope.rememberMe = true;
        $scope.login = function () {
            $rootScope.loginMessage = '';
            $rootScope.hasLoginMessage = false;
            $rootScope.authenticationError = false;
            AuthSharedService.login(
                $scope.username,
                $scope.password,
                $scope.rememberMe
            );
        }
    })
    .controller('SignupController', function ($rootScope, $scope, AuthSharedService) {
        $rootScope.authenticationError = false;
        $rootScope.authenticationErrorMessage = '';

        $scope.signup = function () {
            AuthSharedService.signup(
                $scope.firstname,
                $scope.lastname,
                $scope.email,
                $scope.password
            );
        }
    })
    .controller('RegistrationConfirmController', function ($rootScope, $scope, $location, AuthSharedService, registrationConfirmResponse) {

        if(registrationConfirmResponse.data.code === 200) {
            $scope.hasRegistrationConfirmMessage = true;
            $scope.registrationConfirmMessage = registrationConfirmResponse.data.message;
        } else {
            $scope.registrationConfirmError = true;
            $scope.registrationConfirmErrorMessage = registrationConfirmResponse.data.message;
        }
    })
    .controller('ResetPasswordController', function ($rootScope, $scope, AuthSharedService) {
        $scope.resetpassword = function () {
            $rootScope.passwordResetError = false;
            $rootScope.hasPasswordResetMessage = false;
            $rootScope.passwordResetMessage = '';
            $rootScope.passwordResetErrorMessage = '';
            AuthSharedService.resetpassword(
                $scope.email
            );
        };
    })
    .controller('ResetPasswordUrlController', function ($rootScope, $scope, $routeParams, $location, AuthSharedService, resetPasswordResponse) {
        if(resetPasswordResponse.data.code === 200) {
            $location.path('/changepassword').replace();
        } else {
            $rootScope.passwordResetError = true;
            $rootScope.passwordResetErrorMessage = resetPasswordResponse.data.message;
        }
    })
    .controller('ChangePasswordController', function ($scope, AuthSharedService) {
        $scope.passwordError = false;
        $scope.updatepassword = function () {
            if($scope.newpassword !== $scope.confirmpassword) {
                $scope.passwordError = true;
            } else {
                AuthSharedService.changepassword(
                    $scope.newpassword
                );
            }
        };
    })
    .controller('HomeController', function ($scope, HomeService) {
        $scope.technos = HomeService.getTechno();
    })
    .controller('UsersController', function ($scope, $log, UsersService) {
        $scope.users = UsersService.getAll();
    })
    .controller('ApiDocController', function ($scope) {
        // init form
        $scope.isLoading = false;
        $scope.url = $scope.swaggerUrl = 'v2/api-docs';
        // error management
        $scope.myErrorHandler = function (data, status) {
            console.log('failed to load swagger: ' + status + '   ' + data);
        };

        $scope.infos = false;
    })
    .controller('TokensController', function ($scope, UsersService, TokensService, $q) {

        var browsers = ["Firefox", 'Chrome', 'Trident']

        $q.all([
            UsersService.getAll().$promise,
            TokensService.getAll().$promise
        ]).then(function (data) {
            var users = data[0];
            var tokens = data[1];

            tokens.forEach(function (token) {
                users.forEach(function (user) {
                    if (token.userLogin === user.login) {
                        token.firstName = user.firstName;
                        token.familyName = user.familyName;
                        browsers.forEach(function (browser) {
                            if (token.userAgent.indexOf(browser) > -1) {
                                token.browser = browser;
                            }
                        });
                    }
                });
            });

            $scope.tokens = tokens;
        });


    })
    .controller('LogoutController', function (AuthSharedService) {
        AuthSharedService.logout();
    })
    .controller('ErrorController', function ($scope, $routeParams) {
        $scope.code = $routeParams.code;

        switch ($scope.code) {
            case "403" :
                $scope.message = "Oops! you have come to unauthorised page."
                break;
            case "404" :
                $scope.message = "Page not found."
                break;
            default:
                $scope.code = 500;
                $scope.message = "Oops! unexpected error"
        }

    });