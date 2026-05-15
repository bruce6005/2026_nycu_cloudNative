import React, { useState, useEffect } from 'react';
import { createRequest } from '../api/requestApi';
import { fetchAllRecipes } from '../../recipe/api/recipeApi';
import type { Recipe } from '../../recipe/model/Recipe';

interface RequestFormProps {
    userId: number;
    onSuccess: () => void;
}

export const RequestForm: React.FC<RequestFormProps> = ({ userId, onSuccess }) => {
    const [title, setTitle] = useState('');
    const [priority, setPriority] = useState<'NORMAL' | 'URGENT'>('NORMAL');
    const [description, setDescription] = useState('');
    const [samples, setSamples] = useState([{ barcode: '', recipeId: 0 }]);
    const [allRecipes, setAllRecipes] = useState<Recipe[]>([]);

    useEffect(() => {
        const loadRecipes = async () => {
            try {
                const data = await fetchAllRecipes();
                setAllRecipes(data);
                if (data.length > 0) {
                    setSamples([{ barcode: '', recipeId: data[0].id }]);
                }
            } catch (err) {
                console.error("Failed to load recipes", err);
            }
        };
        loadRecipes();
    }, []);

    const handleAddSample = () => {
        setSamples([...samples, { barcode: '', recipeId: allRecipes[0]?.id || 0 }]);
    };

    const handleRemoveSample = (index: number) => {
        if (samples.length > 1) {
            setSamples(samples.filter((_, i) => i !== index));
        }
    };

    const handleSampleChange = (index: number, field: 'barcode' | 'recipeId', value: string | number) => {
        const newSamples = [...samples];
        (newSamples[index] as any)[field] = value;
        setSamples(newSamples);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (samples.some(s => !s.barcode || !s.recipeId)) {
            alert('請填寫所有樣本代碼與選擇配方');
            return;
        }

        try {
            await createRequest({
                title,
                factoryUserId: userId,
                priority,
                description,
                samples: samples.map(s => ({
                    barcode: s.barcode,
                    recipeId: s.recipeId
                }))
            });
            alert('建立成功！');
            setTitle('');
            setPriority('NORMAL');
            setDescription('');
            setSamples([{ barcode: '', recipeId: allRecipes[0]?.id || 0 }]);
            onSuccess();
        } catch (error: any) {
            alert('建立失敗: ' + (error.message || '請檢查後台連線'));
        }
    };

    return (
        <div className="card mb-2" style={{ maxWidth: '500px' }}>
            <h3 className="mb-1">建立新委託</h3>
            <form onSubmit={handleSubmit} className="column">
                <div className="form-group">
                    <label className="label">委託單標題 (必填)</label>
                    <input
                        placeholder="請輸入標題..."
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                </div>

                <div className="form-group">
                    <label className="label">優先度</label>
                    <div style={{ display: 'flex', gap: '16px', padding: '8px 0' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer' }}>
                            <input
                                type="radio"
                                name="priority"
                                checked={priority === 'NORMAL'}
                                onChange={() => setPriority('NORMAL')}
                            />
                            NORMAL
                        </label>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer' }}>
                            <input
                                type="radio"
                                name="priority"
                                checked={priority === 'URGENT'}
                                onChange={() => setPriority('URGENT')}
                            />
                            URGENT
                        </label>
                    </div>
                </div>

                <div className="form-group">
                    <label className="label">詳細描述</label>
                    <textarea
                        placeholder="請輸入描述..."
                        rows={2}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>

                <div className="form-group">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                        <label className="label" style={{ marginBottom: 0 }}>樣本清單</label>
                        <button type="button" onClick={handleAddSample} className="button" style={{ padding: '2px 8px', fontSize: '12px' }}>
                            + 新增樣本
                        </button>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {samples.map((sample, index) => (
                            <div key={index} style={{
                                display: 'grid',
                                gridTemplateColumns: '1fr 1fr auto',
                                gap: '8px',
                                alignItems: 'center',
                                padding: '8px',
                                border: '1px solid #eee',
                                borderRadius: '4px'
                            }}>
                                <input
                                    placeholder="樣本代碼"
                                    value={sample.barcode}
                                    onChange={(e) => handleSampleChange(index, 'barcode', e.target.value)}
                                    required
                                />
                                <select
                                    value={sample.recipeId}
                                    onChange={(e) => handleSampleChange(index, 'recipeId', Number(e.target.value))}
                                    required
                                >
                                    {allRecipes.map(r => (
                                        <option key={r.id} value={r.id}>{r.name}</option>
                                    ))}
                                </select>
                                <button
                                    type="button"
                                    onClick={() => handleRemoveSample(index)}
                                    style={{ color: 'red', border: 'none', background: 'none', cursor: 'pointer' }}
                                    title="移除"
                                >
                                    ✕
                                </button>
                            </div>
                        ))}
                    </div>
                </div>

                <button type="submit" className="button primary mt-1">
                    建立委託單
                </button>
            </form>
        </div>
    );
};
