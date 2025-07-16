// import { Component, OnInit, inject } from '@angular/core';
// import { ActivatedRoute } from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { RouterModule } from '@angular/router';
// import { FormsModule } from '@angular/forms';
// import { TicketService } from '../service/ticket.service';
// import { QuillModule } from 'ngx-quill';
// import { LinkService } from '../service/link.service';
// import { RelationshipService } from '../service/relationship.service';

// declare var bootstrap: any; // For Bootstrap modals

// @Component({
//   selector: 'app-subtask',
//   standalone: true,
//   imports: [CommonModule, RouterModule, FormsModule, QuillModule],
//   templateUrl: './subtask.component.html',
//   styleUrl: './subtask.component.css'
// })
// export class SubtaskComponent  implements OnInit {
//   ticket: any = null;
//   saving: boolean = false;
//   private descriptionTimer: any;

//   subTickets: any[] = [];
//   newSubTask = {
//     title: '',
//     assign: '',
//     status: '',
//     priority: '',
//     description: '', // For rich text on subticket if needed
//     dateCurrent: new Date().toISOString().substring(0, 10), // Default to current date
//     parentTicket: null as any,
//   };
//   editingSubTask: any = {};
//   deleteSubTicketId: string = '';

//   // New properties for Links & Relationships
//   links: any[] = [];
//   newLink = { url: '', description: '', parentTicket: null as any };
//   deleteLinkId: string = '';

//   relationships: any[] = [];
//   newRelationship = {
//     type: '',
//     targetTicket: null as any,
//     sourceTicket: null as any,
//   };
//   relationshipTypes: string[] = ['Blocking', 'Waiting On', 'Related To'];
//   searchTargetTicketQuery: string = '';
//   searchTicketResults: any[] = [];
//   selectedTargetTicket: any = null;
//   deleteRelationshipId: string = '';

//   private route = inject(ActivatedRoute);
//   private ticketService = inject(TicketService);
//   private linkService = inject(LinkService);
//   private relationshipService = inject(RelationshipService);

//   ngOnInit(): void {
//     const id = this.route.snapshot.paramMap.get('id');
//     if (id) {
//       this.ticketService.getTicketById(id).subscribe({
//         next: (data) => {
//           this.ticket = {
//             id: data.sno,
//             title: data.titleTicket,
//             status: data.status,
//             assignee: data.assign,
//             priority: data.priority,
//             date: data.dateCurrent,
//             description: data.description || '',
//           };
//           this.newSubTask.parentTicket = { sno: this.ticket.id };
//           this.newLink.parentTicket = { sno: this.ticket.id };
//           this.newRelationship.sourceTicket = { sno: this.ticket.id };

//           this.loadSubTickets(id);
//           this.loadLinks(id);
//           this.loadRelationships(id);
//         },
//         error: (err) => {
//           alert('Error fetching ticket');
//           console.error(err);
//         },
//       });
//     }
//   }

//   onContentChanged(event: any): void {
//     this.ticket.description = event.html;
//     clearTimeout(this.descriptionTimer);
//     this.descriptionTimer = setTimeout(() => {
//       this.autoSaveDescription();
//     }, 2000);
//   }

//   autoSaveDescription(): void {
//     if (!this.ticket?.id) return;
//     this.saving = true;

//     this.ticketService
//       .updateDescription(this.ticket.id, this.ticket.description)
//       .subscribe({
//         next: () => {
//           this.saving = false;
//           // Optionally, reload ticket data if you want to see the binary sizes or other derived data immediately
//           // this.ticketService.getTicketById(this.ticket.id).subscribe(data => { /* update local ticket object */ });
//         },
//         error: (err) => {
//           this.saving = false;
//           alert('Error saving description');
//           console.error(err);
//         },
//       });
//   }

//   // --- New Download Methods ---
//   downloadPdf(): void {
//     if (!this.ticket?.id) return;
//     this.ticketService.downloadPdf(this.ticket.id).subscribe({
//       next: (data: Blob) => {
//         const blob = new Blob([data], { type: 'application/pdf' });
//         const url = window.URL.createObjectURL(blob);
//         const a = document.createElement('a');
//         a.href = url;
//         a.download = `ticket_${this.ticket.id}.pdf`;
//         document.body.appendChild(a);
//         a.click();
//         document.body.removeChild(a);
//         window.URL.revokeObjectURL(url);
//       },
//       error: (err) => {
//         alert(
//           'Error downloading PDF. It might not exist or conversion failed.'
//         );
//         console.error(err);
//       },
//     });
//   }

//   downloadDocx(): void {
//     if (!this.ticket?.id) return;
//     this.ticketService.downloadDocx(this.ticket.id).subscribe({
//       next: (data: Blob) => {
//         const blob = new Blob([data], {
//           type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
//         });
//         const url = window.URL.createObjectURL(blob);
//         const a = document.createElement('a');
//         a.href = url;
//         a.download = `ticket_${this.ticket.id}.docx`;
//         document.body.appendChild(a);
//         a.click();
//         document.body.removeChild(a);
//         window.URL.revokeObjectURL(url);
//       },
//       error: (err) => {
//         alert(
//           'Error downloading DOCX. It might not exist or conversion failed.'
//         );
//         console.error(err);
//       },
//     });
//   }

//   // Subtask methods (unchanged from previous version)
//   loadSubTickets(ticketId: string): void {
//     this.ticketService.getAllSubTicketsForTicket(ticketId).subscribe({
//       next: (data) => {
//         this.subTickets = data;
//       },
//       error: (err) => {
//         alert('Error fetching subtasks');
//         console.error(err);
//       },
//     });
//   }

//   createSubTicket(): void {
//     const subTicketToCreate = {
//       ...this.newSubTask,
//       parentTicket: { sno: this.ticket.id },
//     };
//     this.ticketService.createSubTicket(subTicketToCreate).subscribe({
//       next: () => {
//         alert('Subtask created successfully!');
//         this.loadSubTickets(this.ticket.id);
//         this.resetNewSubTaskForm();
//       },
//       error: (err) => {
//         alert('Error creating subtask');
//         console.error(err);
//       },
//     });
//   }

//   openEditSubTicketModal(subTicket: any): void {
//     this.editingSubTask = { ...subTicket };
//     if (this.editingSubTask.dateCurrent) {
//       this.editingSubTask.dateCurrent = new Date(
//         this.editingSubTask.dateCurrent
//       )
//         .toISOString()
//         .substring(0, 10);
//     }
//     const modalEl = document.getElementById('editSubTicketModal');
//     if (modalEl) {
//       const modal = new bootstrap.Modal(modalEl);
//       modal.show();
//     }
//   }

//   updateSubTicket(): void {
//     this.ticketService
//       .updateSubTicket(this.editingSubTask.id, this.editingSubTask)
//       .subscribe({
//         next: () => {
//           alert('Subtask updated successfully!');
//           this.loadSubTickets(this.ticket.id);
//           this.resetEditingSubTaskForm();
//         },
//         error: (err) => {
//           alert('Error updating subtask');
//           console.error(err);
//         },
//       });
//   }

//   confirmDeleteSubTicket(id: string): void {
//     this.deleteSubTicketId = id;
//     const modalEl = document.getElementById('deleteSubTicketConfirmModal');
//     if (modalEl) {
//       const modal = new bootstrap.Modal(modalEl);
//       modal.show();
//     }
//   }

//   deleteSubTicket(): void {
//     this.ticketService.deleteSubTicket(this.deleteSubTicketId).subscribe({
//       next: () => {
//         alert('Subtask deleted successfully!');
//         this.loadSubTickets(this.ticket.id);
//         this.deleteSubTicketId = '';
//       },
//       error: (err) => {
//         alert('Error deleting subtask');
//         console.error(err);
//       },
//     });
//   }

//   resetNewSubTaskForm(): void {
//     this.newSubTask = {
//       title: '',
//       assign: '',
//       status: '',
//       priority: '',
//       description: '',
//       dateCurrent: new Date().toISOString().substring(0, 10),
//       parentTicket: { sno: this.ticket.id },
//     };
//   }

//   resetEditingSubTaskForm(): void {
//     this.editingSubTask = {};
//   }

//   // --- Link Methods (unchanged) ---
//   loadLinks(ticketId: string): void {
//     this.linkService.getLinksByParentTicket(ticketId).subscribe({
//       next: (data) => (this.links = data),
//       error: (err) => {
//         alert('Error fetching links');
//         console.error(err);
//       },
//     });
//   }

//   addLink(): void {
//     if (
//       !this.newLink.url ||
//       (!this.newLink.url.startsWith('http://') &&
//         !this.newLink.url.startsWith('https://'))
//     ) {
//       alert('Please enter a valid URL (must start with http:// or https://)');
//       return;
//     }

//     const linkToCreate = {
//       ...this.newLink,
//       parentTicket: { sno: this.ticket.id },
//     };
//     this.linkService.createLink(linkToCreate).subscribe({
//       next: () => {
//         alert('Link added successfully!');
//         this.loadLinks(this.ticket.id);
//         this.newLink = {
//           url: '',
//           description: '',
//           parentTicket: { sno: this.ticket.id },
//         };
//       },
//       error: (err) => {
//         alert('Error adding link');
//         console.error(err);
//       },
//     });
//   }

//   confirmDeleteLink(linkId: string): void {
//     this.deleteLinkId = linkId;
//     const modalEl = document.getElementById('deleteLinkConfirmModal');
//     if (modalEl) {
//       const modal = new bootstrap.Modal(modalEl);
//       modal.show();
//     }
//   }

//   deleteLink(): void {
//     this.linkService.deleteLink(this.deleteLinkId).subscribe({
//       next: () => {
//         alert('Link deleted successfully!');
//         this.loadLinks(this.ticket.id);
//         this.deleteLinkId = '';
//       },
//       error: (err) => {
//         alert('Error deleting link');
//         console.error(err);
//       },
//     });
//   }

//   // --- Relationship Methods (unchanged) ---
//   loadRelationships(ticketId: string): void {
//     this.relationshipService
//       .getRelationshipsBySourceTicket(ticketId)
//       .subscribe({
//         next: (data) => (this.relationships = data),
//         error: (err) => {
//           alert('Error fetching relationships');
//           console.error(err);
//         },
//       });
//   }

//   searchTickets(): void {
//     if (this.searchTargetTicketQuery.length > 2) {
//       this.ticketService.searchTickets(this.searchTargetTicketQuery).subscribe({
//         next: (data) => (this.searchTicketResults = data),
//         error: (err) => console.error('Error searching tickets', err),
//       });
//     } else {
//       this.searchTicketResults = [];
//     }
//   }

//   selectTargetTicket(ticket: any): void {
//     this.selectedTargetTicket = ticket;
//     this.searchTargetTicketQuery = ticket.titleTicket;
//     this.searchTicketResults = [];
//   }

//   addRelationship(): void {
//     if (!this.newRelationship.type || !this.selectedTargetTicket) {
//       alert('Please select a relationship type and a target ticket.');
//       return;
//     }

//     const relationshipToCreate = {
//       type: this.newRelationship.type,
//       sourceTicket: { sno: this.ticket.id },
//       targetTicket: { sno: this.selectedTargetTicket.sno },
//     };

//     this.relationshipService
//       .createRelationship(relationshipToCreate)
//       .subscribe({
//         next: () => {
//           alert('Relationship added successfully!');
//           this.loadRelationships(this.ticket.id);
//           this.resetNewRelationshipForm();
//         },
//         error: (err) => {
//           alert('Error adding relationship');
//           console.error(err);
//         },
//       });
//   }

//   confirmDeleteRelationship(relationshipId: string): void {
//     this.deleteRelationshipId = relationshipId;
//     const modalEl = document.getElementById('deleteRelationshipConfirmModal');
//     if (modalEl) {
//       const modal = new bootstrap.Modal(modalEl);
//       modal.show();
//     }
//   }

//   deleteRelationship(): void {
//     this.relationshipService
//       .deleteRelationship(this.deleteRelationshipId)
//       .subscribe({
//         next: () => {
//           alert('Relationship deleted successfully!');
//           this.loadRelationships(this.ticket.id);
//           this.deleteRelationshipId = '';
//         },
//         error: (err) => {
//           alert('Error deleting relationship');
//           console.error(err);
//         },
//       });
//   }

//   resetNewRelationshipForm(): void {
//     this.newRelationship = {
//       type: '',
//       targetTicket: null,
//       sourceTicket: { sno: this.ticket.id },
//     };
//     this.searchTargetTicketQuery = '';
//     this.selectedTargetTicket = null;
//     this.searchTicketResults = [];// subtask.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../service/ticket.service';
import { QuillModule } from 'ngx-quill';
import { LinkService } from '../service/link.service';
import { RelationshipService } from '../service/relationship.service';

declare var bootstrap: any; // For Bootstrap modals

@Component({
  selector: 'app-subtask',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, QuillModule],
  templateUrl: './subtask.component.html',
  styleUrl: './subtask.component.css'
})
export class SubtaskComponent implements OnInit {
  ticket: any = null;
  saving: boolean = false;
  private descriptionTimer: any;

  subTickets: any[] = [];
  newSubTask = {
    title: '',
    assign: '',
    status: '',
    priority: '',
    description: '', // For rich text on subticket if needed
    dateCurrent: new Date().toISOString().substring(0, 10), // Default to current date
    parentTicket: null as any,
  };
  editingSubTask: any = {};
  deleteSubTicketId: string = '';

  // New properties for Links & Relationships
  links: any[] = [];
  newLink = { url: '', description: '', parentTicket: null as any };
  deleteLinkId: string = '';

  relationships: any[] = [];
  newRelationship = {
    type: '',
    targetTicket: null as any,
    sourceTicket: null as any,
  };
  relationshipTypes: string[] = ['Blocking', 'Waiting On', 'Related To'];
  searchTargetTicketQuery: string = '';
  searchTicketResults: any[] = [];
  selectedTargetTicket: any = null;
  deleteRelationshipId: string = '';

  private route = inject(ActivatedRoute);
  private ticketService = inject(TicketService);
  private linkService = inject(LinkService);
  private relationshipService = inject(RelationshipService);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.ticketService.getTicketById(id).subscribe({
        next: (data) => {
          this.ticket = {
            id: data.sno,
            title: data.titleTicket,
            status: data.status,
            assignee: data.assign,
            priority: data.priority,
            date: data.dateCurrent,
            description: data.description || '',
          };
          this.newSubTask.parentTicket = { sno: this.ticket.id };
          this.newLink.parentTicket = { sno: this.ticket.id };
          this.newRelationship.sourceTicket = { sno: this.ticket.id };

          this.loadSubTickets(id);
          this.loadLinks(id);
          this.loadRelationships(id);
        },
        error: (err) => {
          alert('Error fetching ticket');
          console.error(err);
        },
      });
    }
  }

  onContentChanged(event: any): void {
    this.ticket.description = event.html;
    clearTimeout(this.descriptionTimer);
    this.descriptionTimer = setTimeout(() => {
      this.autoSaveDescription();
    }, 2000);
  }

  autoSaveDescription(): void {
    if (!this.ticket?.id) return;
    this.saving = true;

    this.ticketService
      .updateDescription(this.ticket.id, this.ticket.description)
      .subscribe({
        next: () => {
          this.saving = false;
          // Optionally, reload ticket data if you want to see the binary sizes or other derived data immediately
          // this.ticketService.getTicketById(this.ticket.id).subscribe(data => { /* update local ticket object */ });
        },
        error: (err) => {
          this.saving = false;
          alert('Error saving description');
          console.error(err);
        },
      });
  }

  // --- New Download Methods ---
  downloadPdf(): void {
    if (!this.ticket?.id) return;
    this.ticketService.downloadPdf(this.ticket.id).subscribe({
      next: (data: Blob) => {
        const blob = new Blob([data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `ticket_${this.ticket.id}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        alert(
          'Error downloading PDF. It might not exist or conversion failed.'
        );
        console.error(err);
      },
    });
  }

  downloadDocx(): void {
    if (!this.ticket?.id) return;
    this.ticketService.downloadDocx(this.ticket.id).subscribe({
      next: (data: Blob) => {
        const blob = new Blob([data], {
          type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `ticket_${this.ticket.id}.docx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        alert(
          'Error downloading DOCX. It might not exist or conversion failed.'
        );
        console.error(err);
      },
    });
  }

  // Subtask methods (unchanged from previous version)
  loadSubTickets(ticketId: string): void {
    this.ticketService.getAllSubTicketsForTicket(ticketId).subscribe({
      next: (data) => {
        this.subTickets = data;
      },
      error: (err) => {
        alert('Error fetching subtasks');
        console.error(err);
      },
    });
  }

  createSubTicket(): void {
    const subTicketToCreate = {
      ...this.newSubTask,
      parentTicket: { sno: this.ticket.id },
    };
    this.ticketService.createSubTicket(subTicketToCreate).subscribe({
      next: () => {
        alert('Subtask created successfully!');
        this.loadSubTickets(this.ticket.id);
        this.resetNewSubTaskForm();
      },
      error: (err) => {
        alert('Error creating subtask');
        console.error(err);
      },
    });
  }

  openEditSubTicketModal(subTicket: any): void {
    this.editingSubTask = { ...subTicket };
    if (this.editingSubTask.dateCurrent) {
      this.editingSubTask.dateCurrent = new Date(
        this.editingSubTask.dateCurrent
      )
        .toISOString()
        .substring(0, 10);
    }
    const modalEl = document.getElementById('editSubTicketModal');
    if (modalEl) {
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    }
  }

  updateSubTicket(): void {
    this.ticketService
      .updateSubTicket(this.editingSubTask.id, this.editingSubTask)
      .subscribe({
        next: () => {
          alert('Subtask updated successfully!');
          this.loadSubTickets(this.ticket.id);
          this.resetEditingSubTaskForm();
        },
        error: (err) => {
          alert('Error updating subtask');
          console.error(err);
        },
      });
  }

  confirmDeleteSubTicket(id: string): void {
    this.deleteSubTicketId = id;
    const modalEl = document.getElementById('deleteSubTicketConfirmModal');
    if (modalEl) {
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    }
  }

  deleteSubTicket(): void {
    this.ticketService.deleteSubTicket(this.deleteSubTicketId).subscribe({
      next: () => {
        alert('Subtask deleted successfully!');
        this.loadSubTickets(this.ticket.id);
        this.deleteSubTicketId = '';
      },
      error: (err) => {
        alert('Error deleting subtask');
        console.error(err);
      },
    });
  }

  resetNewSubTaskForm(): void {
    this.newSubTask = {
      title: '',
      assign: '',
      status: '',
      priority: '',
      description: '',
      dateCurrent: new Date().toISOString().substring(0, 10),
      parentTicket: { sno: this.ticket.id },
    };
  }

  resetEditingSubTaskForm(): void {
    this.editingSubTask = {};
  }

  // --- Link Methods (unchanged) ---
  loadLinks(ticketId: string): void {
    this.linkService.getLinksByParentTicket(ticketId).subscribe({
      next: (data) => (this.links = data),
      error: (err) => {
        alert('Error fetching links');
        console.error(err);
      },
    });
  }

  addLink(): void {
    if (
      !this.newLink.url ||
      (!this.newLink.url.startsWith('http://') &&
        !this.newLink.url.startsWith('https://'))
    ) {
      alert('Please enter a valid URL (must start with http:// or https://)');
      return;
    }

    const linkToCreate = {
      ...this.newLink,
      parentTicket: { sno: this.ticket.id },
    };
    this.linkService.createLink(linkToCreate).subscribe({
      next: () => {
        alert('Link added successfully!');
        this.loadLinks(this.ticket.id);
        this.newLink = {
          url: '',
          description: '',
          parentTicket: { sno: this.ticket.id },
        };
      },
      error: (err) => {
        alert('Error adding link');
        console.error(err);
      },
    });
  }

  confirmDeleteLink(linkId: string): void {
    this.deleteLinkId = linkId;
    const modalEl = document.getElementById('deleteLinkConfirmModal');
    if (modalEl) {
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    }
  }

  deleteLink(): void {
    this.linkService.deleteLink(this.deleteLinkId).subscribe({
      next: () => {
        alert('Link deleted successfully!');
        this.loadLinks(this.ticket.id);
        this.deleteLinkId = '';
      },
      error: (err) => {
        alert('Error deleting link');
        console.error(err);
      },
    });
  }

  // --- Relationship Methods (unchanged) ---
  loadRelationships(ticketId: string): void {
    this.relationshipService
      .getRelationshipsBySourceTicket(ticketId)
      .subscribe({
        next: (data) => (this.relationships = data),
        error: (err) => {
          alert('Error fetching relationships');
          console.error(err);
        },
      });
  }

  searchTickets(): void {
    if (this.searchTargetTicketQuery.length > 2) {
      this.ticketService.searchTickets(this.searchTargetTicketQuery).subscribe({
        next: (data) => (this.searchTicketResults = data),
        error: (err) => console.error('Error searching tickets', err),
      });
    } else {
      this.searchTicketResults = [];
    }
  }

  selectTargetTicket(ticket: any): void {
    this.selectedTargetTicket = ticket;
    this.searchTargetTicketQuery = ticket.titleTicket;
    this.searchTicketResults = [];
  }

  addRelationship(): void {
    if (!this.newRelationship.type || !this.selectedTargetTicket) {
      alert('Please select a relationship type and a target ticket.');
      return;
    }

    const relationshipToCreate = {
      type: this.newRelationship.type,
      sourceTicket: { sno: this.ticket.id },
      targetTicket: { sno: this.selectedTargetTicket.sno },
    };

    this.relationshipService
      .createRelationship(relationshipToCreate)
      .subscribe({
        next: () => {
          alert('Relationship added successfully!');
          this.loadRelationships(this.ticket.id);
          this.resetNewRelationshipForm();
        },
        error: (err) => {
          alert('Error adding relationship');
          console.error(err);
        },
      });
  }

  confirmDeleteRelationship(relationshipId: string): void {
    this.deleteRelationshipId = relationshipId;
    const modalEl = document.getElementById('deleteRelationshipConfirmModal');
    if (modalEl) {
      const modal = new bootstrap.Modal(modalEl);
      modal.show();
    }
  }

  deleteRelationship(): void {
    this.relationshipService
      .deleteRelationship(this.deleteRelationshipId)
      .subscribe({
        next: () => {
          alert('Relationship deleted successfully!');
          this.loadRelationships(this.ticket.id);
          this.deleteRelationshipId = '';
        },
        error: (err) => {
          alert('Error deleting relationship');
          console.error(err);
        },
      });
  }

  resetNewRelationshipForm(): void {
    this.newRelationship = {
      type: '',
      targetTicket: null,
      sourceTicket: { sno: this.ticket.id },
    };
    this.searchTargetTicketQuery = '';
    this.selectedTargetTicket = null;
    this.searchTicketResults = [];
  }


}


