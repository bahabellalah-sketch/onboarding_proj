import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ConfirmationModal from './ConfirmationModal';
import '../styles/DarkRedTheme.css';
import './StepDocumentUpload.css';
import { API_BASE_URL } from '../config/apiConfig';

interface Document {
  id: number;
  etapeId: number;
  filename: string;
  originalFilename: string;
  fileType: string;
  fileSize: number;
  uploadDate: string;
  isSigned: boolean;
  signatureDate?: string;
  uploadedByName: string;
  signedByUserId?: number;
  signedByUserEmail?: string;
  signedByUserName?: string;
  signatureIpAddress?: string;
  documentHash?: string;
  signatureUserAgent?: string;
  signatureType?: string;
}

interface StepDocumentUploadProps {
  etapeId: number;
  etapeNom: string;
  assignmentId: number;
  onDocumentUploaded?: (document: Document) => void;
  isAdmin?: boolean;
  unlocked?: boolean;
}

const StepDocumentUpload: React.FC<StepDocumentUploadProps> = ({ etapeId, etapeNom, assignmentId, onDocumentUploaded, isAdmin, unlocked = true }) => {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [confirmationData, setConfirmationData] = useState<{
    type: 'sign' | 'delete';
    documentId: number;
    documentName: string;
  } | null>(null);
  const [showUpload, setShowUpload] = useState(false);

  useEffect(() => {
    fetchDocuments();
  }, [etapeId]);

  const fetchDocuments = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_BASE_URL}/documents/etape/${etapeId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setDocuments(response.data);
    } catch (err) {
      // Silently handle error for now
      console.error('Error fetching documents:', err);
    }
  };

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // Validate file type
      const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
      if (!allowedTypes.includes(file.type)) {
        setError('Type de fichier non autorisé. Veuillez sélectionner un fichier PDF, DOC ou DOCX.');
        return;
      }

      const maxBytes = 100 * 1024 * 1024; // aligned with backend (100MB)
      if (file.size > maxBytes) {
        setError('La taille du fichier ne doit pas dépasser 100 Mo.');
        return;
      }

      setSelectedFile(file);
      setError('');
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError('Veuillez sélectionner un fichier');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const token = localStorage.getItem('token');
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('etapeId', etapeId.toString());
      
      await axios.post(`${API_BASE_URL}/documents/upload`, formData, {
        headers: {
          Authorization: `Bearer ${token}`
        },
        timeout: 300000
      });

      setSuccess('Document téléchargé avec succès!');
      setSelectedFile(null);
      setShowUpload(false);
      fetchDocuments();
      
      if (onDocumentUploaded) {
        // Refresh would trigger parent to update
        onDocumentUploaded(documents[0]); // Placeholder
      }
    } catch (err: any) {
      const data = err.response?.data;
      const message =
        typeof data === 'string'
          ? data
          : data?.message != null
            ? String(data.message)
            : 'Erreur lors du téléchargement';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const confirmAction = async () => {
    if (!confirmationData) return;
    
    const { type, documentId } = confirmationData;
    
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      if (type === 'sign') {
        await axios.post(
          `${API_BASE_URL}/documents/${documentId}/sign`,
          {},
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setSuccess('Document signé avec succès!');
      } else if (type === 'delete') {
        await axios.delete(`${API_BASE_URL}/documents/${documentId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setSuccess('Document supprimé avec succès!');
      }
      
      fetchDocuments();
    } catch (error: any) {
      console.error('Error:', error);
      setError(type === 'sign' ? 'Erreur lors de la signature du document.' : 'Erreur lors de la suppression du document.');
    } finally {
      setLoading(false);
      setShowConfirmation(false);
      setConfirmationData(null);
    }
  };
  
  const cancelAction = () => {
    setShowConfirmation(false);
    setConfirmationData(null);
  };

  const handleSign = async (documentId: number) => {
    const document = documents.find(doc => doc.id === documentId);
    if (!document) return;
    
    setConfirmationData({
      type: 'sign',
      documentId,
      documentName: document.originalFilename
    });
    setShowConfirmation(true);
  };

  const handleDelete = async (documentId: number) => {
    const document = documents.find(doc => doc.id === documentId);
    if (!document) return;
    
    setConfirmationData({
      type: 'delete',
      documentId,
      documentName: document.originalFilename
    });
    setShowConfirmation(true);
  };

  const handleDownload = async (documentId: number, filename: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_BASE_URL}/documents/${documentId}/download`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      setError('Erreur lors du téléchargement');
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatFileType = (fileType: string): string => {
    const typeMap: { [key: string]: string } = {
      'application/pdf': 'PDF',
      'application/msword': 'DOC',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'DOCX',
      'application/vnd.ms-excel': 'XLS',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'XLSX',
      'application/vnd.ms-powerpoint': 'PPT',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PPTX',
      'text/plain': 'TXT',
      'image/jpeg': 'JPG',
      'image/png': 'PNG',
      'image/gif': 'GIF'
    };
    
    return typeMap[fileType] || fileType.split('/')[1]?.toUpperCase() || fileType;
  };

  return (
    <div className={`step-document-upload ${!unlocked ? 'locked' : ''}`}>
      <div className="step-document-header">
        <h4>📁 Documents - {etapeNom}</h4>
        <button 
          onClick={() => setShowUpload(!showUpload)}
          className="btn btn-info btn-sm"
          disabled={!unlocked}
          title={!unlocked ? "L'assignation est en attente - les documents sont verrouillés" : "Ajouter un document"}
        >
          {showUpload ? '➖' : '➕'} Ajouter un document
        </button>
      </div>
      
      {!unlocked && (
        <div className="lock-notice">
          <span className="lock-icon">🔒</span>
          <span className="lock-message">L'assignation est en attente - les documents seront disponibles à la date de début du parcours</span>
        </div>
      )}

      {showUpload && (
        <div className="upload-panel">
          <div className="upload-form">
            <div className="file-input-wrapper">
              <input
                type="file"
                id={`file-upload-${etapeId}`}
                accept=".pdf,.doc,.docx"
                onChange={handleFileSelect}
                className="form-input"
              />
              <label htmlFor={`file-upload-${etapeId}`} className="file-input-label">
                {selectedFile ? selectedFile.name : 'Choisir un fichier'}
              </label>
            </div>
            
            {selectedFile && (
              <div className="file-info">
                <span className="file-type">Type: {formatFileType(selectedFile.type)}</span>
                <span className="file-size">Taille: {formatFileSize(selectedFile.size)}</span>
              </div>
            )}
            
            <div className="upload-actions">
              <button
                onClick={handleUpload}
                disabled={!selectedFile || loading}
                className="btn btn-success btn-sm"
              >
                {loading ? '⏳ Téléchargement...' : '⬆️ Télécharger'}
              </button>
              <button
                onClick={() => {
                  setSelectedFile(null);
                  setShowUpload(false);
                }}
                className="btn btn-secondary btn-sm"
              >
                ❌ Annuler
              </button>
            </div> 
          </div>
          
          <div className="upload-info">
            <span>📄 Formats: PDF, DOC, DOCX (max. 100 Mo)</span>
          </div>
        </div>
      )}

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {documents.length > 0 && (
        <div className="documents-list">
          <h5>Documents ({documents.length})</h5>
          <div className="document-items">
            {documents.map((doc) => (
              <div key={doc.id} className="document-item">
                <div className="document-info">
                  <span className="document-name">{doc.originalFilename}</span>
                  <span className="document-meta">
                    {formatFileType(doc.fileType)} {formatFileSize(doc.fileSize)} {new Date(doc.uploadDate).toLocaleDateString()}
                  </span>
                  <span className={`document-status ${doc.isSigned ? 'signed' : 'unsigned'}`}>
                    {doc.isSigned ? 'Signé' : 'À signer'}
                  </span>
                  {doc.isSigned && doc.signatureDate && (
                    <span className="signature-date">
                      Signé le {new Date(doc.signatureDate).toLocaleDateString()} à {new Date(doc.signatureDate).toLocaleTimeString()}
                    </span>
                  )}
                </div>
                {doc.isSigned && (
                  <div className="signature-details">
                    <div className="signature-info">
                      <strong>Signature Électronique Simple (SES)</strong>
                      {doc.signedByUserName && <p>Signé par: {doc.signedByUserName}</p>}
                      {doc.signedByUserEmail && <p>Email: {doc.signedByUserEmail}</p>}
                      {doc.signatureIpAddress && <p>Adresse IP: {doc.signatureIpAddress}</p>}
                      {doc.documentHash && (
                        <p className="document-hash">
                          Hash: <code>{doc.documentHash.substring(0, 16)}...</code>
                        </p>
                      )}
                    </div>
                  </div>
                )}
                <div className="document-actions">
                  <button
                    onClick={() => handleDownload(doc.id, doc.originalFilename)}
                    className="btn btn-primary btn-sm"
                    title="Télécharger"
                  >
                    ⬇️
                  </button>
                  {!doc.isSigned && (
                    <button
                      onClick={() => handleDelete(doc.id)}
                      disabled={loading || !unlocked}
                      className="btn btn-danger btn-sm"
                      title={!unlocked ? "Document verrouillé - l'assignation est en attente" : "Supprimer"}
                    >
                      🗑️
                    </button>
                  )}
                  {!doc.isSigned && isAdmin && (
                    <button
                      onClick={() => handleSign(doc.id)}
                      disabled={loading}
                      className="btn btn-warning btn-sm"
                      title="Signer"
                    >
                      ✍️
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {documents.length === 0 && !showUpload && (
        <div className="no-documents">
          <p>Aucun document pour cette étape</p>
        </div>
      )}
      
      <ConfirmationModal
        isOpen={showConfirmation}
        title={confirmationData?.type === 'sign' ? 'Signer le document' : 'Supprimer le document'}
        message={confirmationData?.type === 'sign' 
          ? `Êtes-vous sûr de vouloir signer le document "${confirmationData?.documentName}"? Cette action est irréversible.`
          : `Êtes-vous sûr de vouloir supprimer le document "${confirmationData?.documentName}"? Cette action est irréversible.`
        }
        confirmText={confirmationData?.type === 'sign' ? 'Signer' : 'Supprimer'}
        cancelText="Annuler"
        onConfirm={confirmAction}
        onCancel={cancelAction}
        type={confirmationData?.type === 'sign' ? 'warning' : 'danger'}
        icon={confirmationData?.type === 'sign' ? '✍️' : '🗑️'}
      />
    </div>
  );
};

export default StepDocumentUpload;
