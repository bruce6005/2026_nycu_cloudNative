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
        <div className="card" style={{ maxWidth: '600px', margin: '24px' }}>
            <div className="flex" style={{ justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2 style={{ margin: 0 }}>委託單詳情 - #{request.id}</h2>
                <button className="button secondary" onClick={onBack}>
                    ← 返回列表
                </button>
            </div>

            <div className="column">
                <div className="form-group">
                    <label className="label">標題</label>
                    <div style={{ fontSize: '18px', fontWeight: 'bold' }}>{request.title}</div>
                </div>

                <div className="flex mb-2">
                    <div className="form-group" style={{ flex: 1 }}>
                        <label className="label">目前狀態</label>
                        <div>
                            <span className={`tag ${request.status.toLowerCase()}`}>
                                {request.status}
                            </span>
                        </div>
                    </div>
                    <div className="form-group" style={{ flex: 1 }}>
                        <label className="label">優先度</label>
                        <div>
                            <span className="tag" style={{ background: '#eee' }}>
                                {request.priority}
                            </span>
                        </div>
                    </div>
                </div>

                <div className="form-group">
                    <label className="label">詳細描述</label>
                    <div className="card" style={{ background: '#fcfcfc', minHeight: '100px' }}>
                        {request.description || <span className="text-muted">(無詳細描述)</span>}
                    </div>
                </div>
            </div>
        </div>
    );
};
