import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { authApi } from '../services/api';

// Estados posibles
const AuthStates = {
  LOADING: 'loading',
  AUTHENTICATED: 'authenticated',
  UNAUTHENTICATED: 'unauthenticated',
};

// Acciones del reducer
const AuthActions = {
  SET_LOADING: 'SET_LOADING',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  LOGOUT: 'LOGOUT',
  UPDATE_USER: 'UPDATE_USER',
  SET_ERROR: 'SET_ERROR',
};

// Estado inicial
const initialState = {
  status: AuthStates.LOADING,
  user: null,
  token: null,
  refreshToken: null,
  permissions: [],
  roles: [],
  error: null,
};

// Reducer para manejar el estado de autenticación
const authReducer = (state, action) => {
  switch (action.type) {
    case AuthActions.SET_LOADING:
      return {
        ...state,
        status: AuthStates.LOADING,
        error: null,
      };

    case AuthActions.LOGIN_SUCCESS:
      return {
        ...state,
        status: AuthStates.AUTHENTICATED,
        user: action.payload.user,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        permissions: action.payload.permissions || [],
        roles: action.payload.roles || [],
        error: null,
      };

    case AuthActions.LOGOUT:
      return {
        ...initialState,
        status: AuthStates.UNAUTHENTICATED,
      };

    case AuthActions.UPDATE_USER:
      return {
        ...state,
        user: { ...state.user, ...action.payload },
      };

    case AuthActions.SET_ERROR:
      return {
        ...state,
        status: AuthStates.UNAUTHENTICATED,
        error: action.payload,
      };

    default:
      return state;
  }
};

// Crear contexto
const AuthContext = createContext();

// Provider del contexto
export const AuthProvider = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Verificar autenticación al cargar la aplicación
  useEffect(() => {
    checkAuthStatus();
  }, []);

  // Verificar si hay un token válido en localStorage
  const checkAuthStatus = async () => {
    try {
      const token = localStorage.getItem('token');
      const refreshToken = localStorage.getItem('refreshToken');
      const userData = localStorage.getItem('user');

      if (!token || !refreshToken || !userData) {
        dispatch({ type: AuthActions.LOGOUT });
        return;
      }

      // Validar token con el servidor
      const isValid = await validateToken(token);
      
      if (isValid) {
        const user = JSON.parse(userData);
        dispatch({
          type: AuthActions.LOGIN_SUCCESS,
          payload: {
            user: {
              id: user.userId,
              username: user.username,
              email: user.email || '',
              userType: user.userType,
            },
            token,
            refreshToken,
            permissions: user.permissions || [],
            roles: user.roles || [],
          },
        });
      } else {
        // Intentar refrescar el token
        await refreshAuthToken();
      }
    } catch (error) {
      console.error('Error checking auth status:', error);
      dispatch({ type: AuthActions.LOGOUT });
    }
  };

  // Validar token
  const validateToken = async (token) => {
    try {
      const response = await authApi.validateToken(token);
      return response.data === true;
    } catch (error) {
      return false;
    }
  };

  // Refrescar token
  const refreshAuthToken = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await authApi.refreshToken(refreshToken);
      const authData = response.data;

      // Guardar nuevos tokens
      localStorage.setItem('token', authData.token);
      localStorage.setItem('refreshToken', authData.refreshToken);
      localStorage.setItem('user', JSON.stringify(authData));

      dispatch({
        type: AuthActions.LOGIN_SUCCESS,
        payload: {
          user: {
            id: authData.userId,
            username: authData.username,
            email: authData.email || '',
            userType: authData.userType,
          },
          token: authData.token,
          refreshToken: authData.refreshToken,
          permissions: authData.permissions || [],
          roles: authData.roles || [],
        },
      });

      return true;
    } catch (error) {
      console.error('Error refreshing token:', error);
      dispatch({ type: AuthActions.LOGOUT });
      return false;
    }
  };

  // Función de login
  const login = async (credentials) => {
    try {
      dispatch({ type: AuthActions.SET_LOADING });

      const response = await authApi.login(credentials);
      const authData = response.data;

      // Guardar en localStorage
      localStorage.setItem('token', authData.token);
      localStorage.setItem('refreshToken', authData.refreshToken);
      localStorage.setItem('user', JSON.stringify(authData));

      dispatch({
        type: AuthActions.LOGIN_SUCCESS,
        payload: {
          user: {
            id: authData.userId,
            username: authData.username,
            email: authData.email || '',
            userType: authData.userType,
          },
          token: authData.token,
          refreshToken: authData.refreshToken,
          permissions: authData.permissions || [],
          roles: authData.roles || [],
        },
      });

      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Error de autenticación';
      dispatch({
        type: AuthActions.SET_ERROR,
        payload: errorMessage,
      });
      return { success: false, error: errorMessage };
    }
  };

  // Función de logout
  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch (error) {
      console.error('Error during logout:', error);
    } finally {
      // Limpiar localStorage
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      dispatch({ type: AuthActions.LOGOUT });
    }
  };

  // Verificar si el usuario tiene un permiso específico
  const hasPermission = (permission) => {
    return state.permissions.includes(permission);
  };

  // Verificar si el usuario tiene un rol específico
  const hasRole = (role) => {
    return state.roles.includes(role) || state.roles.includes(`ROLE_${role}`);
  };

  // Verificar si es super admin
  const isSuperAdmin = () => {
    return hasRole('SUPER_ADMIN') || hasRole('ROLE_SUPER_ADMIN');
  };

  const value = {
    ...state,
    login,
    logout,
    hasPermission,
    hasRole,
    isSuperAdmin,
    refreshToken: refreshAuthToken,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook personalizado para usar el contexto
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export { AuthStates };
export default AuthContext;