type Props = {
  title: string;
  value: number | string;
};

function StatCard({ title, value }: Props) {
  return (
    <div className="card dashboard-stat-card">
      <div className="dashboard-stat-title">{title}</div>
      <div className="dashboard-stat-value">{value}</div>
    </div>
  );
}

export default StatCard;