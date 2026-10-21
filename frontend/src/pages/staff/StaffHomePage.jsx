import { Link } from 'react-router-dom';

/** 07 スタッフホーム */
export default function StaffHomePage() {
  return (
    <>
      <h1>スタッフホーム</h1>
      <div className="menu">
        <Link to="/staff/students" className="menu-item">生徒一覧<small>生徒の登録・編集・退会</small></Link>
        <Link to="/staff/slots" className="menu-item">予約枠管理<small>枠の登録・編集・受付停止</small></Link>
        <Link to="/staff/reservations" className="menu-item">予約管理<small>参加者の確認・予約取消</small></Link>
      </div>
    </>
  );
}
