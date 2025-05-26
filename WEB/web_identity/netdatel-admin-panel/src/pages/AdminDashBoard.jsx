import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardApi, clientsApi } from '../../src/services/adminApi';
import { 
  Building2, 
  Package, 
  Users, 
  Activity, 
  TrendingUp, 
  Clock,
  UserCheck,
  AlertTriangle,
  Plus,
  Eye,
  MoreVertical,
  Calendar,
  MapPin
} from 'lucide-react';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalClients: 0,
    activeClients: 0,
    inactiveClients: 0,
    suspendedClients: 0,
  });
  const [moduleDistribution, setModuleDistribution] = useState([]);
  const [recentClients, setRecentClients] = useState([]);
  const [allClients, setAllClients] = useState([]);
  const [clientsPage, setClientsPage] = useState(0);
  const [showAllClients, setShowAllClients] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Cargar datos del dashboard
      const [summaryResponse, distributionResponse, recentResponse] = await Promise.all([
        dashboardApi.getClientSummary(),
        dashboardApi.getModuleDistribution(),
        dashboardApi.getRecentClients(5)
      ]);

      setStats(summaryResponse.data);
      setModuleDistribution(distributionResponse.data);
      setRecentClients(recentResponse.data);

      // Si el usuario quiere ver todos los clientes, cargarlos
      if (showAllClients) {
        const allClientsResponse = await clientsApi.getAll({ page: clientsPage, size: 20 });
        setAllClients(allClientsResponse.data.content || allClientsResponse.data);
      }

    } catch (error) {
      console.error('Error loading dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadAllClients = async () => {
    try {
      setLoading(true);
      setShowAllClients(true);
      const response = await clientsApi.getAll({ page: 0, size: 50 });
      setAllClients(response.data.content || response.data);
    } catch (error) {
      console.error('Error loading all clients:', error);
      setAllClients([]);
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, icon: Icon, color, trend, description }) => (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-3xl font-bold text-gray-900 mt-1">{value}</p>
          {trend && (
            <div className="flex items-center mt-2 text-sm">
              <TrendingUp className="w-4 h-4 text-green-500 mr-1" />
              <span className="text-green-600">+{trend}% este mes</span>
            </div>
          )}
          {description && (
            <p className="text-xs text-gray-500 mt-1">{description}</p>
          )}
        </div>
        <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  );

  const QuickAction = ({ title, description, icon: Icon, onClick, color }) => (
    <button
      onClick={onClick}
      className="card hover:shadow-soft-lg transition-shadow duration-200 text-left group"
    >
      <div className="flex items-start space-x-4">
        <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${color} group-hover:scale-110 transition-transform duration-200`}>
          <Icon className="w-5 h-5 text-white" />
        </div>
        <div>
          <h3 className="font-medium text-gray-900 group-hover:text-primary-700 transition-colors">
            {title}
          </h3>
          <p className="text-sm text-gray-600 mt-1">{description}</p>
        </div>
      </div>
    </button>
  );

  const ClientCard = ({ client }) => (
    <div className="card hover:shadow-soft-lg transition-shadow duration-200">
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-4">
          <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
            <Building2 className="w-6 h-6 text-primary-600" />
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="text-lg font-medium text-gray-900 truncate">
              {client.businessName}
            </h3>
            <p className="text-sm text-gray-600">RUC: {client.ruc}</p>
            {client.commercialName && (
              <p className="text-sm text-gray-500">{client.commercialName}</p>
            )}
            
            <div className="flex items-center space-x-4 mt-3">
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                client.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                client.status === 'INACTIVE' ? 'bg-gray-100 text-gray-800' :
                'bg-red-100 text-red-800'
              }`}>
                {client.status}
              </span>
              
              {client.modules && client.modules.length > 0 && (
                <div className="flex items-center text-blue-600">
                  <Package className="w-4 h-4 mr-1" />
                  <span className="text-xs">{client.modules.length} módulos</span>
                </div>
              )}
              
              <div className="flex items-center text-gray-500">
                <Calendar className="w-4 h-4 mr-1" />
                <span className="text-xs">
                  {new Date(client.registrationDate).toLocaleDateString('es-ES')}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div className="relative">
          <button
            onClick={() => navigate(`/clients/${client.id}`)}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
          >
            <Eye className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );

  if (loading && !showAllClients) {
    return (
      <div className="animate-fade-in">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-20 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-xl text-white p-8">
        <h1 className="text-3xl font-bold mb-2">
          ¡Bienvenido al Dashboard de Administración!
        </h1>
        <p className="text-primary-100 text-lg">
          Gestiona clientes, módulos y supervisa el estado del sistema
        </p>
        <div className="mt-4 text-sm text-primary-200">
          <span>Usuario: {user?.username}</span>
          <span className="mx-2">•</span>
          <span>Último acceso: Hoy</span>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Total Clientes"
          value={stats.totalClients}
          icon={Building2}
          color="bg-blue-500"
          trend={12}
          description="Clientes registrados"
        />
        <StatCard
          title="Clientes Activos"
          value={stats.activeClients}
          icon={UserCheck}
          color="bg-green-500"
          trend={8}
          description="Con servicios activos"
        />
        <StatCard
          title="Clientes Inactivos"
          value={stats.inactiveClients}
          icon={Users}
          color="bg-gray-500"
          description="Temporalmente suspendidos"
        />
        <StatCard
          title="Suspendidos"
          value={stats.suspendedClients}
          icon={AlertTriangle}
          color="bg-red-500"
          description="Requieren atención"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Quick Actions */}
        <div className="lg:col-span-2">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Acciones Rápidas</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <QuickAction
              title="Registrar Cliente"
              description="Agregar un nuevo cliente al sistema"
              icon={Plus}
              color="bg-blue-500"
              onClick={() => navigate('/clients/new')}
            />
            <QuickAction
              title="Gestionar Clientes"
              description="Ver y administrar todos los clientes"
              icon={Building2}
              color="bg-purple-500"
              onClick={() => navigate('/clients')}
            />
            <QuickAction
              title="Módulo 1"
              description="Clientes con Módulo de Documentos"
              icon={Package}
              color="bg-green-500"
              onClick={() => navigate('/modules/module-1')}
            />
            <QuickAction
              title="Módulo 2"
              description="Clientes con Módulo Avanzado"
              icon={Package}
              color="bg-orange-500"
              onClick={() => navigate('/modules/module-2')}
            />
            <QuickAction
              title="Módulo 3"
              description="Clientes con Módulo de Proveedores"
              icon={Package}
              color="bg-red-500"
              onClick={() => navigate('/modules/module-3')}
            />
            <QuickAction
              title="Ver Notificaciones"
              description="Revisar notificaciones y alertas"
              icon={Activity}
              color="bg-indigo-500"
              onClick={() => navigate('/notifications')}
            />
          </div>
        </div>

        {/* Module Distribution */}
        <div>
          <h2 className="text-xl font-bold text-gray-900 mb-6">Distribución de Módulos</h2>
          <div className="card">
            <div className="space-y-4">
              {moduleDistribution.map((module) => (
                <div key={module.moduleId} className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-primary-100 rounded-lg flex items-center justify-center">
                      <Package className="w-4 h-4 text-primary-600" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {module.moduleName}
                      </p>
                      <p className="text-xs text-gray-500">{module.moduleCode}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-bold text-gray-900">
                      {module.activeSubscriptions}
                    </p>
                    <p className="text-xs text-gray-500">clientes</p>
                  </div>
                </div>
              ))}
              
              {moduleDistribution.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                  <Package className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                  <p>No hay datos de distribución</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Recent Clients */}
      <div>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-900">Clientes Recientes</h2>
          <button
            onClick={loadAllClients}
            className="btn-secondary text-sm"
          >
            Ver Todos los Clientes
          </button>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {recentClients.map(client => (
            <ClientCard key={client.id} client={client} />
          ))}
        </div>
        
        {recentClients.length === 0 && !loading && (
          <div className="text-center py-12">
            <Building2 className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No hay clientes recientes</h3>
            <p className="text-gray-600 mb-6">Los nuevos clientes aparecerán aquí</p>
            <button
              onClick={() => navigate('/clients/new')}
              className="btn-primary"
            >
              Registrar Primer Cliente
            </button>
          </div>
        )}
      </div>

      {/* All Clients Section */}
      {showAllClients && (
        <div>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-bold text-gray-900">
              Todos los Clientes ({allClients.length})
            </h2>
            <button
              onClick={() => setShowAllClients(false)}
              className="btn-secondary text-sm"
            >
              Ocultar Lista Completa
            </button>
          </div>
          
          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="card animate-pulse">
                  <div className="h-32 bg-gray-200 rounded"></div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {allClients.map(client => (
                <ClientCard key={client.id} client={client} />
              ))}
            </div>
          )}
          
          {allClients.length === 0 && !loading && (
            <div className="text-center py-12">
              <Building2 className="w-12 h-12 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No hay clientes registrados</h3>
              <p className="text-gray-600 mb-6">Comienza registrando tu primer cliente</p>
              <button
                onClick={() => navigate('/clients/new')}
                className="btn-primary"
              >
                Registrar Cliente
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;