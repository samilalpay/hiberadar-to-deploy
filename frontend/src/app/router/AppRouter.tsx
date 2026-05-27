import { Navigate, Route, Routes } from 'react-router-dom'
import { PanelLayout } from '@/app/layouts/PanelLayout'
import { PublicLayout } from '@/app/layouts/PublicLayout'
import { RequireAuth, RequireRole } from '@/app/router/guards'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { RegisterPage } from '@/features/auth/pages/RegisterPage'
import { AdminApplicationsPage } from '@/features/admin/pages/AdminApplicationsPage'
import { AdminDashboardPage } from '@/features/admin/pages/AdminDashboardPage'
import { AdminFirmRegistrationsPage } from '@/features/admin/pages/AdminFirmRegistrationsPage'
import { AdminGrantsPage } from '@/features/admin/pages/AdminGrantsPage'
import { AdminIngestPage } from '@/features/admin/pages/AdminIngestPage'
import { AdminInstitutionsPage } from '@/features/admin/pages/AdminInstitutionsPage'
import { AdminPreAnalysisPage } from '@/features/admin/pages/AdminPreAnalysisPage'
import { AdminMeetingsPage } from '@/features/admin/pages/AdminMeetingsPage'
import { AdminNotificationsPage } from '@/features/admin/pages/AdminNotificationsPage'
import { FirmApplicationsPage } from '@/features/firm/pages/FirmApplicationsPage'
import { FirmDashboardPage } from '@/features/firm/pages/FirmDashboardPage'
import { FirmGrantDetailPage } from '@/features/firm/pages/FirmGrantDetailPage'
import { FirmGrantsPage } from '@/features/firm/pages/FirmGrantsPage'
import { FirmMeetingsPage } from '@/features/firm/pages/FirmMeetingsPage'
import { FirmMatchesPage } from '@/features/firm/pages/FirmMatchesPage'
import { FirmNotificationsPage } from '@/features/firm/pages/FirmNotificationsPage'
import { FirmPreAnalysisPage } from '@/features/firm/pages/FirmPreAnalysisPage'
import { FirmProfilePage } from '@/features/firm/pages/FirmProfilePage'
import { LandingPage } from '@/features/public/pages/LandingPage'
import { PublicGrantDetailPage } from '@/features/public/pages/PublicGrantDetailPage'
import { PublicGrantsPage } from '@/features/public/pages/PublicGrantsPage'
import { RobotPage } from '@/features/public/pages/RobotPage'

export function AppRouter() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<LandingPage />} />
        <Route path="/robot" element={<RobotPage />} />
        <Route path="/grants" element={<PublicGrantsPage mode="PUBLISHED_ONLY" />} />
        <Route path="/grants/:id" element={<PublicGrantDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route
        path="/app"
        element={
          <RequireAuth>
            <RequireRole roles={['FIRMA']}>
              <PanelLayout />
            </RequireRole>
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to="/app/dashboard" replace />} />
        <Route path="dashboard" element={<FirmDashboardPage />} />
        <Route path="robot" element={<RobotPage />} />
        <Route path="robot/results" element={<PublicGrantsPage mode="PUBLISHED_ONLY" />} />
        <Route path="profile" element={<FirmProfilePage />} />
        <Route path="grants" element={<FirmGrantsPage />} />
        <Route path="grants/:id" element={<FirmGrantDetailPage />} />
        <Route path="grants/matches" element={<FirmMatchesPage />} />
        <Route path="applications" element={<FirmApplicationsPage />} />
        <Route path="meetings" element={<FirmMeetingsPage />} />
        <Route path="notifications" element={<FirmNotificationsPage />} />
        <Route path="pre-analysis/history" element={<FirmPreAnalysisPage />} />
        <Route path="*" element={<Navigate to="/app/dashboard" replace />} />
      </Route>

      <Route
        path="/admin"
        element={
          <RequireAuth>
            <RequireRole roles={['ADMIN', 'TEKNOPARK']}>
              <PanelLayout />
            </RequireRole>
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="firm-registrations" element={<AdminFirmRegistrationsPage />} />
        <Route path="institutions" element={<AdminInstitutionsPage />} />
        <Route path="grants" element={<AdminGrantsPage />} />
        <Route path="grants/public" element={<PublicGrantsPage mode="ALL" />} />
        <Route path="applications" element={<AdminApplicationsPage />} />
        <Route path="meetings" element={<AdminMeetingsPage />} />
        <Route path="notifications" element={<AdminNotificationsPage />} />
        <Route path="profile" element={<FirmProfilePage />} />
        <Route path="pre-analysis" element={<AdminPreAnalysisPage />} />
        <Route path="ingest" element={<AdminIngestPage />} />
        <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
