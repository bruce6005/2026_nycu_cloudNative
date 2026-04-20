import React, { useEffect, useState } from 'react';
import { getRequestById } from '../api/requestApi';

interface RequestDetailProps {
  id: number;
  onBack: () => void; // 提供返回列表的功能
}

export const RequestDetail: React.FC<RequestDetailProps> = ({ id, onBack }) => {
  const [request, setRequest] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const data = await getRequestById(id);
        setRequest(data);
      } catch (error) {
        alert("載入詳情失敗");
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  if (loading) return <p>載入中...</p>;
  if (!request) return <p>找不到資料</p>;

  return (
    <div className="card">
      <button onClick={onBack} style={{ marginBottom: '1rem' }}>← 返回清單</button>
      <h2>委託單詳情 - #{request.id}</h2>
      <hr />
      
      <div style={{ lineHeight: '2' }}>
        <p><strong>標題：</strong> {request.title}</p>
        <p><strong>狀態：</strong> 
          <span className="order-tag" style={{ marginLeft: '10px' }}>{request.status}</span>
        </p>
        <p><strong>優先度：</strong> {request.priority}</p>
        <p><strong>工廠人員 ID：</strong> {request.factoryUserId}</p>
        <p><strong>詳細描述：</strong></p>
        <div style={{ padding: '10px', background: '#f9f9f9', borderRadius: '4px', border: '1px solid #eee' }}>
          {request.description || "(無描述)"}
        </div>
      </div>
    </div>
  );
};
