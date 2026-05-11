import React, { useState } from 'react';
import { RequestDetail } from './RequestDetail';
import type { RequestListItemDTO } from '../api/requestApi';

// 定義每一筆委託單的資料格式
interface RequestListProps {
    requests: RequestListItemDTO[]; // 從外部傳入的委託單清單
}

export const RequestList: React.FC<RequestListProps> = ({ requests }) => {
    const [expandedId, setExpandedId] = useState<number | null>(null);

    const toggleExpanded = (id: number) => {
        setExpandedId((current) => (current === id ? null : id));
    };

    return (
        <div className="card request-list-panel">
            <h3 className="mb-1">委託單列表</h3>
            {requests.length === 0 ? (
                <p className="text-muted">目前沒有任何委託單</p>
            ) : (
                <div className="column request-list-scroll">
                    {requests.map((req) => (
                        <div 
                            key={req.id} 
                            className={`list-item column request-card ${expandedId === req.id ? 'expanded' : ''}`} 
                            style={{ borderBottom: '1px solid var(--border)', paddingBottom: '12px', marginBottom: '8px' }}
                        >
                            <div className="flex" style={{ justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                                <strong style={{ fontSize: '15px' }}>#{req.id} - {req.title}</strong>
                                <div className="flex" style={{ gap: '8px', alignItems: 'center' }}>
                                    <span className={`tag ${req.status.toLowerCase()}`}>
                                        {req.status}
                                    </span>
                                    <button
                                        type="button"
                                        className="button secondary request-expand-button"
                                        onClick={() => toggleExpanded(req.id)}
                                    >
                                        {expandedId === req.id ? '收合' : '展開'}
                                    </button>
                                </div>
                            </div>
                            <div className="text-muted mt-1" style={{ fontSize: '13px', display: 'flex', alignItems: 'center' }}>
                                <span className="tag" style={{ 
                                    marginRight: '8px', 
                                    background: req.priority === 'URGENT' ? '#fee2e2' : '#f3f4f6',
                                    color: req.priority === 'URGENT' ? '#991b1b' : '#374151',
                                    border: req.priority === 'URGENT' ? '1px solid #fecaca' : '1px solid #d1d5db'
                                }}>
                                    {req.priority}
                                </span>
                                <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {req.description}
                                </span>
                            </div>

                            {expandedId === req.id && (
                                <div className="request-expanded-detail">
                                    <RequestDetail id={req.id} inline />
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};
