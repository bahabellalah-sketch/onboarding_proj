import React, { ReactNode } from 'react';
import './PageHeader.css';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

const PageHeader: React.FC<PageHeaderProps> = ({ title, subtitle, actions }) => (
  <header className="page-header">
    <div className="page-header__text">
      <h1 className="page-header__title">{title}</h1>
      {subtitle && <p className="page-header__subtitle">{subtitle}</p>}
    </div>
    {actions && <div className="page-header__actions">{actions}</div>}
  </header>
);

export default PageHeader;
