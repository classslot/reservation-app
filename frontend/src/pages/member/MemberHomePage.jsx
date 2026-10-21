import { Link } from 'react-router-dom';
import { useAuth } from '../../lib/AuthContext.jsx';

/** 02 会員ホーム */
export default function MemberHomePage() {
  const { student } = useAuth();
  return (
    <>
      <h1>{student ? `${student.name} さんのホーム` : '会員ホーム'}</h1>
      <div className="menu">
        <Link to="/member/slots" className="menu-item">予約枠一覧<small>空き枠を確認して予約</small></Link>
        <Link to="/member/reservations" className="menu-item">自分の予約<small>予約の確認・取消</small></Link>
        <Link to="/member/profile" className="menu-item">登録情報<small>コース・レベルの確認</small></Link>
      </div>
    </>
  );
}
