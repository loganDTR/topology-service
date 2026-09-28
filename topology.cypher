CREATE CONSTRAINT asset_id_unique IF NOT EXISTS
  FOR (a:Asset) REQUIRE a.id IS UNIQUE;

CREATE CONSTRAINT service_id_unique IF NOT EXISTS
  FOR (s:Service) REQUIRE s.id IS UNIQUE;

CREATE CONSTRAINT application_id_unique IF NOT EXISTS
  FOR (a:Application) REQUIRE a.id IS UNIQUE;

CREATE CONSTRAINT team_id_unique IF NOT EXISTS
  FOR (t:Team) REQUIRE t.id IS UNIQUE;

CREATE CONSTRAINT bf_id_unique IF NOT EXISTS
  FOR (b:BusinessFunction) REQUIRE b.id IS UNIQUE;

CREATE CONSTRAINT uj_id_unique IF NOT EXISTS
  FOR (u:UserJourney) REQUIRE u.id IS UNIQUE;

// ============================================================
// PHYSICAL LAYER
// ============================================================
MERGE (aks:Asset:AKSCluster {
  id: 'asset-aks-prod-01',
  name: 'aks-prod-01',
  region: 'westeurope',
  tier: 'production'
})

MERGE (vnet:Asset:VNet {
  id: 'asset-vnet-banking-prod',
  name: 'vnet-banking-prod',
  cidr: '10.0.0.0/16'
})

MERGE (subnet_app:Asset:Subnet {
  id: 'asset-subnet-app',
  name: 'subnet-app',
  cidr: '10.0.1.0/24',
  purpose: 'application'
})

MERGE (subnet_db:Asset:Subnet {
  id: 'asset-subnet-db',
  name: 'subnet-db',
  cidr: '10.0.2.0/24',
  purpose: 'database'
})

MERGE (vm_pg:Asset:VirtualMachine {
  id: 'asset-vm-postgres-01',
  name: 'vm-postgres-01',
  os: 'Ubuntu 22.04',
  cpu: 8,
  ram_gb: 32
})

MERGE (vm_redis:Asset:VirtualMachine {
  id: 'asset-vm-redis-01',
  name: 'vm-redis-01',
  os: 'Ubuntu 22.04',
  cpu: 4,
  ram_gb: 16
})

MERGE (db_main:Asset:Database {
  id: 'asset-db-postgres-banking-prod',
  name: 'postgres-banking-prod',
  type: 'PostgreSQL',
  version: '15'
})

MERGE (db_cache:Asset:Database {
  id: 'asset-db-redis-session-prod',
  name: 'redis-session-prod',
  type: 'Redis',
  version: '7'
})

MERGE (apigw:Asset:APIGateway {
  id: 'asset-apigw-banking-prod',
  name: 'apim-banking-prod',
  type: 'Azure API Management'
})

MERGE (lb:Asset:LoadBalancer {
  id: 'asset-lb-aks-ingress',
  name: 'lb-aks-ingress',
  type: 'nginx-ingress'
})

MERGE (aks)-[:CONNECTED_TO]->(vnet)
MERGE (vnet)-[:CONTAINS]->(subnet_app)
MERGE (vnet)-[:CONTAINS]->(subnet_db)
MERGE (vm_pg)-[:ATTACHED_TO]->(subnet_db)
MERGE (vm_redis)-[:ATTACHED_TO]->(subnet_db)
MERGE (db_main)-[:HOSTED_ON]->(vm_pg)
MERGE (db_cache)-[:HOSTED_ON]->(vm_redis)
MERGE (apigw)-[:ROUTES_TO]->(lb)
MERGE (lb)-[:FRONTS]->(aks)

// ============================================================
// LOGICAL / FUNCTIONAL LAYER
// ============================================================
MERGE (svc_auth:Service {
  id: 'svc-auth',
  name: 'auth-service',
  version: '2.3.1'
})

MERGE (svc_account:Service {
  id: 'svc-account',
  name: 'account-service',
  version: '1.8.0'
})

MERGE (svc_payment:Service {
  id: 'svc-payment',
  name: 'payment-service',
  version: '3.1.0'
})

MERGE (svc_card:Service {
  id: 'svc-card',
  name: 'card-service',
  version: '1.2.5'
})

MERGE (svc_notif:Service {
  id: 'svc-notification',
  name: 'notification-service',
  version: '1.5.0'
})

MERGE (svc_mortgage:Service {
  id: 'svc-mortgage',
  name: 'mortgage-service',
  version: '1.0.3'
})

// Deploy su infrastruttura
MERGE (svc_auth)-[:DEPLOYED_ON]->(aks)
MERGE (svc_account)-[:DEPLOYED_ON]->(aks)
MERGE (svc_payment)-[:DEPLOYED_ON]->(aks)
MERGE (svc_card)-[:DEPLOYED_ON]->(aks)
MERGE (svc_notif)-[:DEPLOYED_ON]->(aks)
MERGE (svc_mortgage)-[:DEPLOYED_ON]->(aks)

// Dipendenze da DB con proprietà di criticità
MERGE (svc_auth)-[:USES {
  purpose: 'session',
  required: true,
  criticality: 'high',
  dependencyType: 'cache'
}]->(db_cache)

MERGE (svc_auth)-[:USES {
  purpose: 'user_store',
  required: true,
  criticality: 'high',
  dependencyType: 'data'
}]->(db_main)

MERGE (svc_account)-[:USES {
  purpose: 'account_data',
  required: true,
  criticality: 'high',
  dependencyType: 'data'
}]->(db_main)

MERGE (svc_payment)-[:USES {
  purpose: 'transaction_data',
  required: true,
  criticality: 'high',
  dependencyType: 'data'
}]->(db_main)

MERGE (svc_card)-[:USES {
  purpose: 'card_data',
  required: true,
  criticality: 'high',
  dependencyType: 'data'
}]->(db_main)

MERGE (svc_mortgage)-[:USES {
  purpose: 'mortgage_data',
  required: true,
  criticality: 'high',
  dependencyType: 'data'
}]->(db_main)

// Dipendenze inter-servizio con semantica più forte
MERGE (svc_payment)-[:DEPENDS_ON {
  reason: 'auth_validation',
  dependencyType: 'auth',
  required: true,
  criticality: 'high'
}]->(svc_auth)

MERGE (svc_payment)-[:DEPENDS_ON {
  reason: 'balance_check',
  dependencyType: 'data',
  required: true,
  criticality: 'high'
}]->(svc_account)

MERGE (svc_card)-[:DEPENDS_ON {
  reason: 'auth_validation',
  dependencyType: 'auth',
  required: true,
  criticality: 'high'
}]->(svc_auth)

MERGE (svc_notif)-[:DEPENDS_ON {
  reason: 'trigger',
  dependencyType: 'business-event',
  required: true,
  criticality: 'medium'
}]->(svc_payment)

MERGE (svc_notif)-[:DEPENDS_ON {
  reason: 'trigger',
  dependencyType: 'business-event',
  required: true,
  criticality: 'medium'
}]->(svc_card)

MERGE (svc_mortgage)-[:DEPENDS_ON {
  reason: 'auth_validation',
  dependencyType: 'auth',
  required: true,
  criticality: 'high'
}]->(svc_auth)

// Business Functions
MERGE (bf_login:BusinessFunction {
  id: 'bf-login',
  name: 'Login'
})

MERGE (bf_account:BusinessFunction {
  id: 'bf-account-overview',
  name: 'AccountOverview'
})

MERGE (bf_bonifico:BusinessFunction {
  id: 'bf-bonifico',
  name: 'Bonifico'
})

MERGE (bf_card:BusinessFunction {
  id: 'bf-gestione-carta',
  name: 'GestioneCarta'
})

MERGE (bf_notif:BusinessFunction {
  id: 'bf-notifiche',
  name: 'Notifiche'
})

MERGE (bf_mortgage:BusinessFunction {
  id: 'bf-mutuo-status',
  name: 'MutuoStatus'
})

// Opzione 2 mantenuta: BusinessFunction -> IMPLEMENTED_BY -> Service
MERGE (bf_login)-[:IMPLEMENTED_BY]->(svc_auth)
MERGE (bf_account)-[:IMPLEMENTED_BY]->(svc_account)
MERGE (bf_account)-[:IMPLEMENTED_BY]->(svc_auth)
MERGE (bf_bonifico)-[:IMPLEMENTED_BY]->(svc_payment)
MERGE (bf_bonifico)-[:IMPLEMENTED_BY]->(svc_auth)
MERGE (bf_card)-[:IMPLEMENTED_BY]->(svc_card)
MERGE (bf_card)-[:IMPLEMENTED_BY]->(svc_auth)
MERGE (bf_notif)-[:IMPLEMENTED_BY]->(svc_notif)
MERGE (bf_mortgage)-[:IMPLEMENTED_BY]->(svc_mortgage)
MERGE (bf_mortgage)-[:IMPLEMENTED_BY]->(svc_auth)

// ============================================================
// APPLICATION LAYER
// ============================================================
MERGE (app_mobile:Application {
  id: 'app-mobile-banking',
  name: 'MobileBankingApp',
  type: 'mobile',
  platform: 'iOS/Android'
})

MERGE (app_web:Application {
  id: 'app-web-banking-portal',
  name: 'WebBankingPortal',
  type: 'web'
})

MERGE (uj_login:UserJourney {
  id: 'uj-login',
  name: 'Login',
  channel: 'both'
})

MERGE (uj_bonifico:UserJourney {
  id: 'uj-bonifico',
  name: 'Bonifico',
  channel: 'both'
})

MERGE (uj_saldo:UserJourney {
  id: 'uj-saldo-movimenti',
  name: 'SaldoMovimenti',
  channel: 'both'
})

MERGE (uj_card:UserJourney {
  id: 'uj-gestione-carta',
  name: 'GestioneCarta',
  channel: 'both'
})

MERGE (uj_mutuo:UserJourney {
  id: 'uj-consultazione-mutuo',
  name: 'ConsultazioneMutuo',
  channel: 'web'
})

MERGE (app_mobile)-[:EXPOSES]->(uj_login)
MERGE (app_mobile)-[:EXPOSES]->(uj_bonifico)
MERGE (app_mobile)-[:EXPOSES]->(uj_saldo)
MERGE (app_mobile)-[:EXPOSES]->(uj_card)

MERGE (app_web)-[:EXPOSES]->(uj_login)
MERGE (app_web)-[:EXPOSES]->(uj_bonifico)
MERGE (app_web)-[:EXPOSES]->(uj_saldo)
MERGE (app_web)-[:EXPOSES]->(uj_card)
MERGE (app_web)-[:EXPOSES]->(uj_mutuo)

MERGE (uj_login)-[:REQUIRES]->(bf_login)
MERGE (uj_bonifico)-[:REQUIRES]->(bf_login)
MERGE (uj_bonifico)-[:REQUIRES]->(bf_bonifico)
MERGE (uj_bonifico)-[:REQUIRES]->(bf_notif)
MERGE (uj_saldo)-[:REQUIRES]->(bf_login)
MERGE (uj_saldo)-[:REQUIRES]->(bf_account)
MERGE (uj_card)-[:REQUIRES]->(bf_login)
MERGE (uj_card)-[:REQUIRES]->(bf_card)
MERGE (uj_mutuo)-[:REQUIRES]->(bf_login)
MERGE (uj_mutuo)-[:REQUIRES]->(bf_mortgage)

// ============================================================
// TEAM (ownership strutturale, non operativa)
// ============================================================
MERGE (team_infra:Team {
  id: 'team-infra',
  name: 'InfraTeam',
  type: 'internal',
  scope: 'infrastructure'
})

MERGE (team_core:Team {
  id: 'team-core-banking-dev',
  name: 'CoreBankingDev',
  type: 'internal',
  scope: 'development'
})

MERGE (team_digital:Team {
  id: 'team-digital-channels-dev',
  name: 'DigitalChannelsDev',
  type: 'external',
  vendor: 'Accenture'
})

MERGE (team_ops:Team {
  id: 'team-operations',
  name: 'OperationsTeam',
  type: 'internal',
  scope: 'operations'
})

MERGE (team_infra)-[:MANAGES]->(aks)
MERGE (team_infra)-[:MANAGES]->(vm_pg)
MERGE (team_infra)-[:MANAGES]->(vm_redis)

MERGE (team_core)-[:MANAGES]->(svc_payment)
MERGE (team_core)-[:MANAGES]->(svc_account)
MERGE (team_core)-[:MANAGES]->(svc_mortgage)

MERGE (team_digital)-[:MANAGES]->(app_mobile)
MERGE (team_digital)-[:MANAGES]->(app_web)
MERGE (team_digital)-[:MANAGES]->(svc_auth)
MERGE (team_digital)-[:MANAGES]->(svc_card)

MERGE (team_ops)-[:MANAGES]->(apigw)