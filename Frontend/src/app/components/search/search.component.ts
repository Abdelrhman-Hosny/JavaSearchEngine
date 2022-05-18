import {Component, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, FormBuilder, FormControl, FormGroup, NgForm} from "@angular/forms";
import {SearchService} from "../../search.service";
import {Observable} from "rxjs";
import {map,startWith} from 'rxjs/operators'
import {EventEmitter} from "@angular/core";

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],

  viewProviders:[
    {
      provide:ControlContainer,useExisting:NgForm


    }

  ]
})
export class SearchComponent implements OnInit {
  @Input() currentTermValue: any;
  @Input() rightIcon;
  @Output()valuechosen:EventEmitter<string>=new EventEmitter<string>();
  options = ["ahmed","ahmod"];

  constructor(private searchService:SearchService) {
    this.rightIcon = 'true';
    this.valuechosen.emit('');
  }

  myControl = new FormControl();
  filteredOptions!: Observable<string[]>;


  ngOnInit(): void {
    this.filteredOptions = this.myControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filter(value))
    )
  }


  private _filter(value: string): string[] {
    const filterValue = value.toLowerCase();
    this.valuechosen.emit(value);
    if(value == ""){
      return [];
    }

    this.searchService.getAutoCompleteData(value).subscribe(data =>{
      // assigning options to retrieven auto completes
      this.options = data.list;
    })

    return this.options.filter(option => option.toLowerCase().includes(filterValue));


  }
}
