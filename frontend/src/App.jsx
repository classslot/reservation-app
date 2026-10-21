import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import RequireRole from './components/RequireRole.jsx';
import { useAuth, homePath } from './lib/AuthContext.jsx';
import LoginPage from './pages/LoginPage.jsx';
import MemberHomePage from './pages/member/MemberHomePage.jsx';
import ProfilePage from './pages/member/ProfilePage.jsx';
import SlotListPage from './pages/member/SlotListPage.jsx';
import SlotDetailPage from './pages/member/SlotDetailPage.jsx';
import MyReservationsPage from './pages/member/MyReservationsPage.jsx';
import StaffHomePage from './pages/staff/StaffHomePage.jsx';
import StudentListPage from './pages/staff/StudentListPage.jsx';
import StudentDetailPage from './pages/staff/StudentDetailPage.jsx';
import StudentFormPage from './pages/staff/StudentFormPage.jsx';
import SlotManagePage from './pages/staff/SlotManagePage.jsx';
import SlotFormPage from './pages/staff/SlotFormPage.jsx';
import ReservationManagePage from './pages/staff/ReservationManagePage.jsx';

const member = (el) => <RequireRole role="MEMBER">{el}</RequireRole>;
const staff = (el) => <RequireRole role="STAFF">{el}</RequireRole>;

function RootRedirect() {
  const { user, loading } = useAuth();
  if (loading) return null;
  return <Navigate to={user ? homePath(user.role) : '/login'} replace />;
}

/** 画面遷移図・画面設計書のURL構成に対応するルーティング */
export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<RootRedirect />} />
        <Route path="/login" element={<LoginPage />} />

        <Route path="/member" element={member(<MemberHomePage />)} />
        <Route path="/member/profile" element={member(<ProfilePage />)} />
        <Route path="/member/slots" element={member(<SlotListPage />)} />
        <Route path="/member/slots/:id" element={member(<SlotDetailPage />)} />
        <Route path="/member/reservations" element={member(<MyReservationsPage />)} />

        <Route path="/staff" element={staff(<StaffHomePage />)} />
        <Route path="/staff/students" element={staff(<StudentListPage />)} />
        <Route path="/staff/students/new" element={staff(<StudentFormPage mode="new" />)} />
        <Route path="/staff/students/:id" element={staff(<StudentDetailPage />)} />
        <Route path="/staff/students/:id/edit" element={staff(<StudentFormPage mode="edit" />)} />
        <Route path="/staff/slots" element={staff(<SlotManagePage />)} />
        <Route path="/staff/slots/new" element={staff(<SlotFormPage mode="new" />)} />
        <Route path="/staff/slots/:id/edit" element={staff(<SlotFormPage mode="edit" />)} />
        <Route path="/staff/reservations" element={staff(<ReservationManagePage />)} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
