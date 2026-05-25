export type UserRole = 'ADMINISTRATEUR' | 'MANAGER' | 'COLLABORATEUR';

export interface NavItem {
  path: string;
  label: string;
  icon: string;
  roles: UserRole[];
  /** Préfixe de route pour marquer l'élément actif (ex. /profile pour /profile/12) */
  matchPrefix?: boolean;
}

export const NAV_ITEMS: NavItem[] = [
  {
    path: '/dashboard',
    label: 'Tableau de bord',
    icon: '◫',
    roles: ['ADMINISTRATEUR', 'MANAGER'],
  },
  {
    path: '/assignments',
    label: 'Assignations',
    icon: '☑',
    roles: ['ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR'],
  },
  {
    path: '/parcours',
    label: 'Parcours',
    icon: '◎',
    roles: ['ADMINISTRATEUR', 'MANAGER'],
  },
  {
    path: '/team',
    label: 'Mon équipe',
    icon: '👥',
    roles: ['ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR'],
  },
  {
    path: '/analytics',
    label: 'Analytique',
    icon: '▤',
    roles: ['ADMINISTRATEUR', 'MANAGER'],
  },
  {
    path: '/users',
    label: 'Utilisateurs',
    icon: '⚙',
    roles: ['ADMINISTRATEUR'],
  },
  {
    path: '/evaluations',
    label: 'Évaluations',
    icon: '★',
    roles: ['ADMINISTRATEUR'],
  },
  {
    path: '/evaluations/pending',
    label: 'À évaluer',
    icon: '★',
    roles: ['MANAGER'],
  },
  {
    path: '/reports',
    label: 'Signalements',
    icon: '▣',
    roles: ['ADMINISTRATEUR'],
  },
  {
    path: '/notifications',
    label: 'Notifications',
    icon: '🔔',
    roles: ['ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR'],
  },
  {
    path: '/profile',
    label: 'Mon profil',
    icon: '●',
    roles: ['ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR'],
    matchPrefix: true,
  },
];

export function getNavItemsForRole(role?: string): NavItem[] {
  if (!role) return [];
  return NAV_ITEMS.filter((item) => item.roles.includes(role as UserRole));
}

export function getPageTitle(pathname: string): string {
  const exact = NAV_ITEMS.find((item) => item.path === pathname);
  if (exact) return exact.label;
  if (pathname.startsWith('/profile/')) return 'Profil collaborateur';
  if (pathname.startsWith('/profile')) return 'Mon profil';
  if (pathname.startsWith('/evaluations/pending')) return 'À évaluer';
  if (pathname.startsWith('/evaluations')) return 'Évaluations';
  return 'Onboarding';
}
