import React, { useEffect, useState } from 'react';
import { getRequestById, type RequestDetailDTO } from '../api/requestApi';
import '../styles/request.css';

interface RequestDetailProps {
  id: number;
    onBack?: () => void; // 提供返回列表的功能
    inline?: boolean;
}

export const RequestDetail: React.FC<RequestDetailProps> = ({ id, onBack, inline = false }) => {
    const [request, setRequest] = useState<RequestDetailDTO | null>(null);
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
        <div className={`request-detail ${inline ? 'request-detail-inline' : 'card'}`}>
            <div className="flex" style={{ justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', gap: '12px' }}>
                <h2 style={{ margin: 0 }}>委託單詳情 - #{request.id}</h2>
                {!inline && onBack && (
                    <button className="button secondary" onClick={onBack}>
                        ← 返回列表
                    </button>
                )}
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
                    <div className="card" style={{ background: '#fcfcfc', minHeight: '60px', marginBottom: '16px' }}>
                        {request.description || <span className="text-muted">(無詳細描述)</span>}
                    </div>
                </div>

                <div className="form-group">
                    <label className="label">樣本與配方清單</label>
                    <div className="request-sample-list">
                        {request.samples && request.samples.length > 0 ? (
                            request.samples.map((s, idx: number) => (
                                <div key={idx} className="request-sample-card">
                                    <div className="request-sample-main">
                                        <span className="request-sample-barcode">{s.barcode}</span>
                                        <span className="request-sample-recipe">下一步 Recipe：{s.recipeName || '未指定配方'}</span>
                                        <span className="tag request-sample-status">
                                            {s.status || 'UNKNOWN'}
                                        </span>
                                    </div>
                                    <div className="request-sample-meta">
                                        <div className="request-sample-meta-item">
                                            <span className="label-inline">Recipe</span>
                                            <span>{s.recipeName || '未指定配方'}</span>
                                        </div>
                                        <div className="request-sample-meta-item">
                                            <span className="label-inline">Recipe 詳細</span>
                                            <span className="request-sample-params">
                                                {s.recipeParameters || '無參數資訊'}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="text-muted">此單無樣本資料</div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};
