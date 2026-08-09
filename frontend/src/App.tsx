import React, { useState } from 'react';
import { DashboardPage } from './pages/DashboardPage';
import { SimulatorPage } from './pages/SimulatorPage';

export const App: React.FC = () => {
  const [currentPage, setCurrentPage] = useState<'DASHBOARD' | 'SIMULATOR'>('DASHBOARD');

  return (
    <div className="w-screen h-screen bg-space-950 text-slate-100">
      {currentPage === 'DASHBOARD' ? (
        <DashboardPage onOpenSimulator={() => setCurrentPage('SIMULATOR')} />
      ) : (
        <SimulatorPage onBackToDashboard={() => setCurrentPage('DASHBOARD')} />
      )}
    </div>
  );
};
